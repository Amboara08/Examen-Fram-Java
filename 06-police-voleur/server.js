"use strict";

const express = require("express");
const http = require("http");
const os = require("os");
const path = require("path");
const { Server } = require("socket.io");

const PORT = Number(process.env.PORT) || 3000;
const app = express();
const httpServer = http.createServer(app);
const io = new Server(httpServer);
const rooms = new Map();

app.use(express.static(path.join(__dirname, "public")));
app.get("/health", (_req, res) =>
  res.json({ ok: true, service: "police-voleur-lan" }),
);

function buildAdjacency() {
  const adj = Array.from({ length: 21 }, () => []);
  const link = (a, b) => {
    if (!adj[a].includes(b)) adj[a].push(b);
    if (!adj[b].includes(a)) adj[b].push(a);
  };

  for (let i = 0; i < 12; i++) link(i, (i + 1) % 12);

  const mid = { top: 12, right: 13, bottom: 14, left: 15 };
  const inner = { top: 16, right: 17, bottom: 18, left: 19 };
  const center = 20;

  link(inner.top, inner.right);
  link(inner.right, inner.bottom);
  link(inner.bottom, inner.left);
  link(inner.left, inner.top);

  link(0, mid.top);
  link(mid.top, inner.top);
  link(inner.top, center);
  link(center, inner.bottom);
  link(inner.bottom, mid.bottom);
  link(mid.bottom, 6);

  link(3, mid.right);
  link(mid.right, inner.right);
  link(inner.right, center);
  link(center, inner.left);
  link(inner.left, mid.left);
  link(mid.left, 9);

  link(mid.top, 11);
  link(mid.top, 1);
  link(mid.right, 2);
  link(mid.right, 4);
  link(mid.bottom, 5);
  link(mid.bottom, 7);
  link(mid.left, 8);
  link(mid.left, 10);

  return adj;
}

const adjacency = buildAdjacency();
const CENTER_NODE = 20;

function createInitialState() {
  // Le centre reste libre : c'est l'objectif que le voleur doit atteindre.
  // On mélange les 20 autres sommets et on attribue quatre positions distinctes.
  // On recommence si le tirage bloque déjà le voleur ou les trois policiers.
  let pieces;
  let validPlacement = false;

  while (!validPlacement) {
    const availableNodes = Array.from(
      { length: CENTER_NODE },
      (_, index) => index,
    );
    for (let i = availableNodes.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [availableNodes[i], availableNodes[j]] = [
        availableNodes[j],
        availableNodes[i],
      ];
    }

    pieces = [
      { type: "thief", node: availableNodes[0] },
      { type: "police", node: availableNodes[1] },
      { type: "police", node: availableNodes[2] },
      { type: "police", node: availableNodes[3] },
    ];

    const initialState = { pieces };
    const thief = pieces.find((piece) => piece.type === "thief");
    const police = pieces.filter((piece) => piece.type === "police");
    validPlacement =
      getLegalMoves(initialState, thief).length > 0 &&
      police.some((piece) => getLegalMoves(initialState, piece).length > 0);
  }

  return {
    pieces,
    turn: "thief",
    turns: 0,
    thiefMoves: 0,
    gameOver: false,
    winner: null,
    message:
      "Les pions sont placés aléatoirement. Le voleur doit atteindre le centre.",
    lastMove: null,
  };
}

// --- Intelligence artificielle : Minimax + élagage Alpha-Bêta ---
function cloneState(state) {
  return { pieces: state.pieces.map((piece) => ({ ...piece })) };
}

function legalMovesFor(state, piece) {
  const occupied = new Set(
    state.pieces.filter((other) => other !== piece).map((other) => other.node),
  );
  return adjacency[piece.node].filter((node) => !occupied.has(node));
}

function shortestDistance(start, target, blocked) {
  if (start === target) return 0;
  const queue = [[start, 0]];
  const visited = new Set([start]);
  while (queue.length) {
    const [node, distance] = queue.shift();
    for (const next of adjacency[node]) {
      if (blocked.has(next) || visited.has(next)) continue;
      if (next === target) return distance + 1;
      visited.add(next);
      queue.push([next, distance + 1]);
    }
  }
  return 99;
}

// Score positif = avantage au voleur ; score négatif = avantage aux policiers.
function evaluateState(state) {
  const thief = state.pieces.find((piece) => piece.type === "thief");
  const police = state.pieces.filter((piece) => piece.type === "police");
  const thiefMoves = legalMovesFor(state, thief);
  if (thief.node === CENTER_NODE) return 100000;
  if (thiefMoves.length === 0) return -100000;
  if (police.every((piece) => legalMovesFor(state, piece).length === 0))
    return 100000;

  const blocked = new Set(
    state.pieces
      .filter((piece) => piece.type === "police")
      .map((piece) => piece.node),
  );
  const distanceToCenter = shortestDistance(thief.node, CENTER_NODE, blocked);
  const policeDistances = police.map((piece) => {
    const occupiedByOthers = new Set(
      state.pieces
        .filter((other) => other !== piece && other !== thief)
        .map((other) => other.node),
    );
    return shortestDistance(piece.node, thief.node, occupiedByOthers);
  });
  const closestPolice = Math.min(...policeDistances);
  return distanceToCenter * 12 - closestPolice * 4 + thiefMoves.length * 3;
}

function minimax(state, depth, alpha, beta, maximizingThief) {
  const score = evaluateState(state);
  if (depth === 0 || Math.abs(score) >= 100000) return { score };
  const role = maximizingThief ? "thief" : "police";
  const pieces = state.pieces.filter((piece) => piece.type === role);
  let best = maximizingThief ? -Infinity : Infinity;
  let bestMove = null;
  for (const piece of pieces) {
    for (const to of legalMovesFor(state, piece)) {
      const next = cloneState(state);
      const moving = next.pieces.find(
        (item) => item.type === role && item.node === piece.node,
      );
      moving.node = to;
      const result = minimax(next, depth - 1, alpha, beta, !maximizingThief);
      if (
        (maximizingThief && result.score > best) ||
        (!maximizingThief && result.score < best)
      ) {
        best = result.score;
        bestMove = { from: piece.node, to };
      }
      if (maximizingThief) alpha = Math.max(alpha, best);
      else beta = Math.min(beta, best);
      if (beta <= alpha) break;
    }
    if (beta <= alpha) break;
  }
  return { score: best, move: bestMove };
}

function choosePoliceAIMove(state) {
  // Les policiers minimisent le score du voleur. Profondeur 4 :
  // police -> voleur -> police -> voleur.
  const result = minimax(cloneState(state), 4, -Infinity, Infinity, false);
  return result.move;
}

function applyMove(room, role, from, to) {
  const state = room.state;
  const piece = state.pieces.find(
    (item) => item.type === role && item.node === from,
  );
  if (!piece || !Number.isInteger(to) || !adjacency[from]?.includes(to))
    return false;
  if (state.pieces.some((item) => item.node === to)) return false;
  piece.node = to;
  state.lastMove = { type: role, from, to };

  if (role === "thief") {
    state.thiefMoves += 1;
    if (to === CENTER_NODE) {
      state.gameOver = true;
      state.winner = "thief";
      state.turn = "over";
      state.message = "Le voleur a atteint le centre ! Le voleur gagne !";
    } else {
      state.turn = "police";
      state.message = room.aiMode
        ? "L’IA analyse les déplacements…"
        : "Le voleur a joué. Au tour des policiers.";
    }
  } else {
    state.turns += 1;
    state.turn = "thief";
    state.message = room.aiMode
      ? "L’IA a joué. À ton tour !"
      : "Les policiers ont joué. Au tour du voleur.";
  }

  if (!state.gameOver) {
    const thief = state.pieces.find((item) => item.type === "thief");
    if (getLegalMoves(state, thief).length === 0) {
      state.gameOver = true;
      state.winner = "police";
      state.turn = "over";
      state.message = "Le voleur est bloqué. Les policiers gagnent !";
    } else if (isPoliceStuck(state)) {
      state.gameOver = true;
      state.winner = "thief";
      state.turn = "over";
      state.message = "Les policiers ne peuvent plus bouger. Le voleur gagne !";
    }
  }
  return true;
}

function playAIMove(room) {
  if (!room.aiMode || room.state.gameOver || room.state.turn !== "police")
    return;
  const move = choosePoliceAIMove(room.state);
  if (move) applyMove(room, "police", move.from, move.to);
  else {
    room.state.gameOver = true;
    room.state.winner = "thief";
    room.state.turn = "over";
    room.state.message = "Les policiers ne peuvent plus bouger. Tu gagnes !";
  }
  emitRoomUpdate(room);
}

function getRoomCode() {
  const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let code;
  do {
    code = "";
    for (let i = 0; i < 5; i++)
      code += alphabet[Math.floor(Math.random() * alphabet.length)];
  } while (rooms.has(code));
  return code;
}

function getLegalMoves(state, piece) {
  const occupied = new Set(
    state.pieces.filter((other) => other !== piece).map((other) => other.node),
  );
  return adjacency[piece.node].filter((node) => !occupied.has(node));
}

function isPoliceStuck(state) {
  return state.pieces
    .filter((piece) => piece.type === "police")
    .every((piece) => getLegalMoves(state, piece).length === 0);
}

function snapshot(room) {
  return {
    pieces: room.state.pieces.map((piece) => ({ ...piece })),
    turn: room.state.turn,
    turns: room.state.turns,
    thiefMoves: room.state.thiefMoves,
    gameOver: room.state.gameOver,
    winner: room.state.winner,
    message: room.state.message,
    lastMove: room.state.lastMove ? { ...room.state.lastMove } : null,
    playersOnline: {
      thief: Boolean(room.players.thief),
      police: room.aiMode ? true : Boolean(room.players.police),
    },
  };
}

function emitRoomUpdate(room) {
  io.to(room.code).emit("gameUpdate", {
    code: room.code,
    state: snapshot(room),
  });
}

function acknowledge(callback, data) {
  if (typeof callback === "function") callback(data);
}

function releaseSocketFromRoom(socket) {
  const code = socket.data.roomCode;
  const role = socket.data.role;
  if (!code || !rooms.has(code)) return null;

  const room = rooms.get(code);
  if (role && room.players[role] === socket.id) room.players[role] = null;
  socket.leave(code);
  socket.data.roomCode = null;
  socket.data.role = null;

  if (room.players.thief || room.players.police) {
    room.state.message =
      "Un joueur est déconnecté. En attente de sa reconnexion.";
    emitRoomUpdate(room);
  }
  return room;
}

function attachSocket(socket, room, role) {
  room.players[role] = socket.id;
  socket.join(room.code);
  socket.data.roomCode = room.code;
  socket.data.role = role;
  room.state.message =
    room.players.thief && room.players.police
      ? "Les deux joueurs sont connectés. Le voleur commence : atteins le centre !"
      : "En attente du deuxième joueur…";
  emitRoomUpdate(room);
  return {
    ok: true,
    code: room.code,
    role,
    state: snapshot(room),
  };
}

io.on("connection", (socket) => {
  socket.data.roomCode = null;
  socket.data.role = null;

  socket.on("createRoom", (_payload, callback) => {
    releaseSocketFromRoom(socket);
    const code = getRoomCode();
    const room = {
      code,
      players: { thief: null, police: null },
      state: createInitialState(),
    };
    rooms.set(code, room);
    acknowledge(callback, attachSocket(socket, room, "thief"));
  });

  socket.on("createAiRoom", (_payload, callback) => {
    releaseSocketFromRoom(socket);
    const code = getRoomCode();
    const room = {
      code,
      players: { thief: socket.id, police: null },
      aiMode: true,
      state: createInitialState(),
    };
    rooms.set(code, room);
    socket.join(code);
    socket.data.roomCode = code;
    socket.data.role = "thief";
    room.state.message =
      "Tu joues le voleur. L’IA Minimax contrôle les trois policiers.";
    emitRoomUpdate(room);
    acknowledge(callback, {
      ok: true,
      code,
      role: "thief",
      state: snapshot(room),
    });
  });

  socket.on("joinRoom", (payload = {}, callback) => {
    releaseSocketFromRoom(socket);
    const code = String(payload.code || "")
      .trim()
      .toUpperCase();
    const room = rooms.get(code);
    if (!room) {
      acknowledge(callback, {
        ok: false,
        error: "Salle introuvable. Vérifie le code.",
      });
      return;
    }
    const role = !room.players.thief
      ? "thief"
      : !room.players.police
        ? "police"
        : null;
    if (!role) {
      acknowledge(callback, {
        ok: false,
        error: "Cette salle contient déjà deux joueurs.",
      });
      return;
    }
    acknowledge(callback, attachSocket(socket, room, role));
  });

  socket.on("rejoinRoom", (payload = {}, callback) => {
    const code = String(payload.code || "")
      .trim()
      .toUpperCase();
    const role = payload.role;
    const room = rooms.get(code);
    if (!room || !["thief", "police"].includes(role)) {
      acknowledge(callback, {
        ok: false,
        error: "La salle n’existe plus. Crée une nouvelle partie.",
      });
      return;
    }
    if (room.players[role] && room.players[role] !== socket.id) {
      acknowledge(callback, {
        ok: false,
        error: "Ce rôle est déjà occupé par un autre joueur.",
      });
      return;
    }
    releaseSocketFromRoom(socket);
    acknowledge(callback, attachSocket(socket, room, role));
  });

  socket.on("move", (payload = {}, callback) => {
    const code = socket.data.roomCode;
    const role = socket.data.role;
    const room = rooms.get(code);
    if (!room || !role) {
      acknowledge(callback, {
        ok: false,
        error: "Tu ne fais partie d’aucune salle.",
      });
      return;
    }
    if (!room.players.thief || (!room.aiMode && !room.players.police)) {
      acknowledge(callback, {
        ok: false,
        error: "La partie attend la connexion des deux joueurs.",
      });
      return;
    }
    const state = room.state;
    if (state.gameOver) {
      acknowledge(callback, {
        ok: false,
        error: "Cette partie est déjà terminée.",
      });
      return;
    }
    if (state.turn !== role) {
      acknowledge(callback, { ok: false, error: "Ce n’est pas ton tour." });
      return;
    }

    const from = Number(payload.from);
    const to = Number(payload.to);
    const piece = state.pieces.find(
      (item) => item.type === role && item.node === from,
    );
    if (!piece || !Number.isInteger(to) || !adjacency[from]?.includes(to)) {
      acknowledge(callback, { ok: false, error: "Déplacement invalide." });
      return;
    }
    if (state.pieces.some((item) => item.node === to)) {
      acknowledge(callback, {
        ok: false,
        error: "Cette intersection est déjà occupée.",
      });
      return;
    }

    if (!applyMove(room, role, from, to)) {
      acknowledge(callback, { ok: false, error: "Déplacement invalide." });
      return;
    }
    if (room.aiMode && !room.state.gameOver && room.state.turn === "police") {
      // Le coup de l’IA est calculé côté serveur ; le LAN humain-humain reste inchangé.
      playAIMove(room);
    } else {
      emitRoomUpdate(room);
    }
    acknowledge(callback, { ok: true });
  });

  socket.on("resetGame", (callback) => {
    const room = rooms.get(socket.data.roomCode);
    if (!room || !socket.data.role) {
      acknowledge(callback, {
        ok: false,
        error: "Tu ne fais partie d’aucune salle.",
      });
      return;
    }
    if (!room.players.thief || (!room.aiMode && !room.players.police)) {
      acknowledge(callback, {
        ok: false,
        error: "Il faut deux joueurs connectés pour commencer.",
      });
      return;
    }
    room.state = createInitialState();
    room.state.message = room.aiMode
      ? "Nouvelle partie ! Tu joues le voleur contre l’IA Minimax."
      : "Nouvelle partie ! Les pions ont été replacés aléatoirement. Le voleur commence.";
    emitRoomUpdate(room);
    acknowledge(callback, { ok: true });
  });

  socket.on("leaveRoom", () => releaseSocketFromRoom(socket));
  socket.on("disconnect", () => releaseSocketFromRoom(socket));
});

function getLanAddresses() {
  const interfaces = os.networkInterfaces();
  const addresses = [];
  for (const entries of Object.values(interfaces)) {
    for (const entry of entries || []) {
      if (entry.family === "IPv4" && !entry.internal)
        addresses.push(entry.address);
    }
  }
  return addresses;
}

httpServer.listen(PORT, "0.0.0.0", () => {
  console.log("\n==========================================");
  console.log("  POLICE ET VOLEUR — SERVEUR LAN");
  console.log("==========================================");
  console.log(`Serveur local : http://localhost:${PORT}`);
  const addresses = getLanAddresses();
  if (addresses.length) {
    console.log("Adresses à utiliser sur les autres appareils :");
    addresses.forEach((address) => console.log(`  http://${address}:${PORT}`));
  } else {
    console.log("Aucune adresse réseau locale détectée.");
  }
  console.log("\nLes appareils doivent être sur le même réseau Wi-Fi.");
  console.log("==========================================\n");
});
