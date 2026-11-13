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

// --- IA stratégique : Minimax + Alpha-Bêta + tri des coups ---
// Le score est toujours exprimé du point de vue du voleur :
// positif = avantage voleur ; négatif = avantage policiers.
function cloneState(state) {
  return { pieces: state.pieces.map((piece) => ({ ...piece })) };
}

function legalMovesFor(state, piece) {
  const occupied = new Set(
    state.pieces.filter((other) => other !== piece).map((other) => other.node),
  );
  return adjacency[piece.node].filter((node) => !occupied.has(node));
}

function shortestDistance(start, target, blocked = new Set()) {
  if (start === target) return 0;
  const queue = [[start, 0]];
  const visited = new Set([start]);
  for (let index = 0; index < queue.length; index++) {
    const [node, distance] = queue[index];
    for (const next of adjacency[node]) {
      if (blocked.has(next) || visited.has(next)) continue;
      if (next === target) return distance + 1;
      visited.add(next);
      queue.push([next, distance + 1]);
    }
  }
  return 99;
}

function evaluateState(state) {
  const thief = state.pieces.find((piece) => piece.type === "thief");
  const police = state.pieces.filter((piece) => piece.type === "police");
  const thiefMoves = legalMovesFor(state, thief);

  if (thief.node === CENTER_NODE) return 100000;
  if (thiefMoves.length === 0) return -100000;
  if (police.every((piece) => legalMovesFor(state, piece).length === 0))
    return 100000;

  const policeNodes = new Set(police.map((piece) => piece.node));
  const distanceToCenter = shortestDistance(
    thief.node,
    CENTER_NODE,
    policeNodes,
  );
  if (distanceToCenter === 99) return -50000;

  const policeDistances = police.map((piece) => {
    const otherPolice = new Set(
      police.filter((other) => other !== piece).map((other) => other.node),
    );
    return shortestDistance(piece.node, thief.node, otherPolice);
  });
  const closestPolice = Math.min(...policeDistances);
  const totalPoliceDistance = policeDistances.reduce(
    (sum, value) => sum + value,
    0,
  );
  const freeCenterEntrances = adjacency[CENTER_NODE].filter(
    (node) => !policeNodes.has(node),
  ).length;

  // Le voleur préfère être proche du centre, garder plusieurs sorties et
  // maintenir les policiers à distance. Les policiers cherchent l'inverse.
  return (
    -distanceToCenter * 24 +
    closestPolice * 9 +
    totalPoliceDistance * 2 +
    thiefMoves.length * 5 +
    freeCenterEntrances * 7
  );
}

function positionKey(state, turn) {
  const thief = state.pieces.find((piece) => piece.type === "thief").node;
  const police = state.pieces
    .filter((piece) => piece.type === "police")
    .map((piece) => piece.node)
    .sort((a, b) => a - b);
  return `${turn}|${thief}|${police.join(",")}`;
}

function generateOrderedMoves(state, turn) {
  const pieces = state.pieces.filter((piece) => piece.type === turn);
  const moves = [];
  for (const piece of pieces) {
    for (const to of legalMovesFor(state, piece)) {
      const next = cloneState(state);
      const moving = next.pieces.find(
        (item) => item.type === turn && item.node === piece.node,
      );
      moving.node = to;
      moves.push({
        from: piece.node,
        to,
        state: next,
        score: evaluateState(next),
      });
    }
  }
  // Explorer d'abord les coups les plus prometteurs améliore fortement
  // l'efficacité de l'élagage Alpha-Bêta.
  moves.sort((a, b) =>
    turn === "thief" ? b.score - a.score : a.score - b.score,
  );
  return moves;
}

function minimax(state, depth, alpha, beta, turn, table, budget) {
  budget.nodes++;
  const score = evaluateState(state);
  if (
    depth === 0 ||
    Math.abs(score) >= 100000 ||
    budget.nodes >= budget.maxNodes
  ) {
    return { score, move: null };
  }

  const key = positionKey(state, turn);
  const cached = table.get(key);
  if (cached && cached.depth >= depth)
    return { score: cached.score, move: cached.move };

  const moves = generateOrderedMoves(state, turn);
  if (!moves.length)
    return { score: turn === "thief" ? -100000 : 100000, move: null };

  const maximizing = turn === "thief";
  let bestScore = maximizing ? -Infinity : Infinity;
  let bestMove = null;
  let fullySearched = true;

  for (const candidate of moves) {
    const result = minimax(
      candidate.state,
      depth - 1,
      alpha,
      beta,
      maximizing ? "police" : "thief",
      table,
      budget,
    );
    if (
      (maximizing && result.score > bestScore) ||
      (!maximizing && result.score < bestScore)
    ) {
      bestScore = result.score;
      bestMove = { from: candidate.from, to: candidate.to };
    }
    if (maximizing) alpha = Math.max(alpha, bestScore);
    else beta = Math.min(beta, bestScore);
    if (beta <= alpha || budget.nodes >= budget.maxNodes) {
      fullySearched = false;
      break;
    }
  }

  // Ne conserver que les résultats exacts dans la table de transposition.
  if (fullySearched)
    table.set(key, { depth, score: bestScore, move: bestMove });
  return { score: bestScore, move: bestMove };
}

function chooseAIMove(state, aiRole) {
  if (state.turn !== aiRole) return null;
  const table = new Map();
  const budget = { nodes: 0, maxNodes: 90000 };
  let bestMove = null;
  // Approfondissement itératif : on garde toujours le meilleur coup déjà trouvé.
  // La profondeur maximale de 10 demi-coups anticipe plusieurs réponses adverses.
  for (let depth = 1; depth <= 10; depth++) {
    const result = minimax(
      cloneState(state),
      depth,
      -Infinity,
      Infinity,
      state.turn,
      table,
      budget,
    );
    if (result.move) bestMove = result.move;
    if (Math.abs(result.score) >= 100000 || budget.nodes >= budget.maxNodes)
      break;
  }
  return bestMove;
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
        ? room.aiRole === "thief"
          ? "L’IA a joué. À toi de déplacer les policiers !"
          : "L’IA analyse les déplacements…"
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
  if (!room.aiMode || room.state.gameOver || room.state.turn !== room.aiRole)
    return;
  const aiRole = room.aiRole;
  const move = chooseAIMove(room.state, aiRole);
  if (move) {
    applyMove(room, aiRole, move.from, move.to);
  } else {
    room.state.gameOver = true;
    room.state.winner = aiRole === "thief" ? "police" : "thief";
    room.state.turn = "over";
    room.state.message =
      aiRole === "thief"
        ? "Le voleur ne peut plus avancer. Les policiers gagnent !"
        : "Les policiers ne peuvent plus bouger. Tu gagnes !";
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
    aiMode: Boolean(room.aiMode),
    aiRole: room.aiRole || null,
    playersOnline: {
      thief: room.aiMode
        ? room.aiRole === "thief" || Boolean(room.players.thief)
        : Boolean(room.players.thief),
      police: room.aiMode
        ? room.aiRole === "police" || Boolean(room.players.police)
        : Boolean(room.players.police),
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

  socket.on("createAiRoom", (payload = {}, callback) => {
    releaseSocketFromRoom(socket);
    const humanRole = payload.role === "police" ? "police" : "thief";
    const aiRole = humanRole === "thief" ? "police" : "thief";
    const code = getRoomCode();
    const players = { thief: null, police: null };
    players[humanRole] = socket.id;
    const room = {
      code,
      players,
      aiMode: true,
      aiRole,
      humanRole,
      state: createInitialState(),
    };
    rooms.set(code, room);
    socket.join(code);
    socket.data.roomCode = code;
    socket.data.role = humanRole;
    room.state.message =
      humanRole === "thief"
        ? "Tu joues le voleur. L’IA Minimax contrôle les trois policiers."
        : "Tu contrôles les trois policiers. L’IA Minimax joue le voleur et cherche le centre.";
    acknowledge(callback, {
      ok: true,
      code,
      role: humanRole,
      state: snapshot(room),
    });
    if (room.state.turn === aiRole) setImmediate(() => playAIMove(room));
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
    if (room.aiMode) {
      acknowledge(callback, {
        ok: false,
        error: "Cette salle est réservée à une partie contre l’IA.",
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
    if (
      !room ||
      !["thief", "police"].includes(role) ||
      (room.aiMode && role !== room.humanRole)
    ) {
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
    const ready = room.aiMode
      ? Boolean(room.players[room.humanRole])
      : Boolean(room.players.thief && room.players.police);
    if (!ready) {
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
    if (
      room.aiMode &&
      !room.state.gameOver &&
      room.state.turn === room.aiRole
    ) {
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
    const ready = room.aiMode
      ? Boolean(room.players[room.humanRole])
      : Boolean(room.players.thief && room.players.police);
    if (!ready) {
      acknowledge(callback, {
        ok: false,
        error: "Il faut que les joueurs soient connectés pour commencer.",
      });
      return;
    }
    room.state = createInitialState();
    room.state.message = room.aiMode
      ? room.humanRole === "thief"
        ? "Nouvelle partie ! Tu joues le voleur contre les policiers Minimax."
        : "Nouvelle partie ! Tu contrôles les policiers contre le voleur Minimax."
      : "Nouvelle partie ! Les pions ont été replacés aléatoirement. Le voleur commence.";
    emitRoomUpdate(room);
    acknowledge(callback, { ok: true });
    if (room.aiMode && room.state.turn === room.aiRole)
      setImmediate(() => playAIMove(room));
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
