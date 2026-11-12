"use strict";

const socket = io();

class Board {
  constructor() {
    this.nodes = [];
    this.edges = [];
    this.adj = [];
    this.buildGraph();
  }

  addNode(x, y, kind = "outer") {
    const id = this.nodes.length;
    this.nodes.push({ id, x, y, kind });
    this.adj[id] = [];
    return id;
  }

  link(a, b) {
    if (a === undefined || b === undefined || this.adj[a].includes(b)) return;
    this.edges.push([a, b]);
    this.adj[a].push(b);
    this.adj[b].push(a);
  }

  buildGraph() {
    const center = 360;
    const outerRadius = 290;
    const innerRadius = 82;
    const outer = [];

    for (let i = 0; i < 12; i++) {
      const angle = ((-90 + i * 30) * Math.PI) / 180;
      outer.push(
        this.addNode(
          center + outerRadius * Math.cos(angle),
          center + outerRadius * Math.sin(angle),
          "outer",
        ),
      );
    }
    for (let i = 0; i < 12; i++) this.link(outer[i], outer[(i + 1) % 12]);

    const mid = {
      top: this.addNode(center, center - outerRadius * 0.55, "junction"),
      right: this.addNode(center + outerRadius * 0.55, center, "junction"),
      bottom: this.addNode(center, center + outerRadius * 0.55, "junction"),
      left: this.addNode(center - outerRadius * 0.55, center, "junction"),
    };
    const inner = {
      top: this.addNode(center, center - innerRadius, "inner"),
      right: this.addNode(center + innerRadius, center, "inner"),
      bottom: this.addNode(center, center + innerRadius, "inner"),
      left: this.addNode(center - innerRadius, center, "inner"),
    };
    const middle = this.addNode(center, center, "center");

    this.link(inner.top, inner.right);
    this.link(inner.right, inner.bottom);
    this.link(inner.bottom, inner.left);
    this.link(inner.left, inner.top);

    this.link(outer[0], mid.top);
    this.link(mid.top, inner.top);
    this.link(inner.top, middle);
    this.link(middle, inner.bottom);
    this.link(inner.bottom, mid.bottom);
    this.link(mid.bottom, outer[6]);

    this.link(outer[3], mid.right);
    this.link(mid.right, inner.right);
    this.link(inner.right, middle);
    this.link(middle, inner.left);
    this.link(inner.left, mid.left);
    this.link(mid.left, outer[9]);

    this.link(mid.top, outer[11]);
    this.link(mid.top, outer[1]);
    this.link(mid.right, outer[2]);
    this.link(mid.right, outer[4]);
    this.link(mid.bottom, outer[5]);
    this.link(mid.bottom, outer[7]);
    this.link(mid.left, outer[8]);
    this.link(mid.left, outer[10]);
  }

  neighbors(node) {
    return this.adj[node] || [];
  }

  draw(ctx, pieces, selected, legal) {
    ctx.clearRect(0, 0, 720, 720);
    ctx.save();
    ctx.lineCap = "round";
    ctx.lineJoin = "round";

    ctx.strokeStyle = "#c7cbd1";
    ctx.lineWidth = 2.2;
    for (const [a, b] of this.edges) {
      const p = this.nodes[a];
      const q = this.nodes[b];
      ctx.beginPath();
      ctx.moveTo(p.x, p.y);
      ctx.lineTo(q.x, q.y);
      ctx.stroke();
    }

    for (const node of this.nodes) {
      // Le sommet central est l'objectif du voleur : on le distingue en doré.
      if (node.kind === "center") {
        ctx.beginPath();
        ctx.arc(node.x, node.y, 26, 0, Math.PI * 2);
        ctx.strokeStyle = "#f2c14e";
        ctx.lineWidth = 3;
        ctx.stroke();
      }

      ctx.beginPath();
      ctx.arc(
        node.x,
        node.y,
        node.kind === "outer" ? 5.8 : 5.5,
        0,
        Math.PI * 2,
      );
      ctx.fillStyle = "#e45b61";
      ctx.fill();
    }

    for (const id of legal) {
      const node = this.nodes[id];
      ctx.beginPath();
      ctx.arc(node.x, node.y, 14, 0, Math.PI * 2);
      ctx.fillStyle = "#4caf5040";
      ctx.fill();
      ctx.strokeStyle = "#83df88";
      ctx.lineWidth = 2.5;
      ctx.stroke();
    }

    if (selected !== null) {
      const node = this.nodes[selected];
      ctx.beginPath();
      ctx.arc(node.x, node.y, 24, 0, Math.PI * 2);
      ctx.strokeStyle = "#fff";
      ctx.lineWidth = 2.5;
      ctx.stroke();
    }

    for (const piece of pieces) {
      const position = this.nodes[piece.node];
      const isThief = piece.type === "thief";
      ctx.beginPath();
      ctx.arc(position.x, position.y, 17.5, 0, Math.PI * 2);
      ctx.fillStyle = isThief ? "#4caf50" : "#2196f3";
      ctx.fill();
      ctx.strokeStyle = isThief ? "#183c1a" : "#123c65";
      ctx.lineWidth = 3;
      ctx.stroke();
      ctx.beginPath();
      ctx.arc(position.x - 5, position.y - 6, 4, 0, Math.PI * 2);
      ctx.fillStyle = "#ffffff35";
      ctx.fill();
    }
    ctx.restore();
  }
}

const board = new Board();
const canvas = document.getElementById("board");
const ctx = canvas.getContext("2d");
const elements = {
  lobby: document.getElementById("lobby"),
  gameView: document.getElementById("gameView"),
  connectionPill: document.getElementById("connectionPill"),
  connectionText: document.getElementById("connectionText"),
  lobbyFeedback: document.getElementById("lobbyFeedback"),
  createRoom: document.getElementById("createRoom"),
  joinRoom: document.getElementById("joinRoom"),
  roomCodeInput: document.getElementById("roomCodeInput"),
  roomCodeDisplay: document.getElementById("roomCodeDisplay"),
  copyCode: document.getElementById("copyCode"),
  myRole: document.getElementById("myRole"),
  myRoleDescription: document.getElementById("myRoleDescription"),
  turnTitle: document.getElementById("turnTitle"),
  statusText: document.getElementById("statusText"),
  turnDot: document.getElementById("turnDot"),
  turnLabel: document.getElementById("turnLabel"),
  thiefPresence: document.getElementById("thiefPresence"),
  policePresence: document.getElementById("policePresence"),
  thiefOnline: document.getElementById("thiefOnline"),
  policeOnline: document.getElementById("policeOnline"),
  turnCount: document.getElementById("turnCount"),
  thiefCount: document.getElementById("thiefCount"),
  message: document.getElementById("message"),
  resetGame: document.getElementById("resetGame"),
  leaveRoom: document.getElementById("leaveRoom"),
};

let myRole = null;
let roomCode = null;
let gameState = null;
let selectedNode = null;
let legalNodes = [];
let movePending = false;
let localNotice = "";

function setConnection(status, message) {
  elements.connectionPill.classList.remove("connected", "disconnected");
  if (status) elements.connectionPill.classList.add(status);
  elements.connectionText.textContent = message;
}

function showLobby(message = "") {
  elements.lobby.classList.remove("hidden");
  elements.gameView.classList.add("hidden");
  if (message) elements.lobbyFeedback.textContent = message;
}

function showGame() {
  elements.lobby.classList.add("hidden");
  elements.gameView.classList.remove("hidden");
  render();
}

function enterRoom(response) {
  if (!response || !response.ok) {
    elements.lobbyFeedback.textContent =
      response?.error || "Impossible de rejoindre cette salle.";
    return;
  }
  roomCode = response.code;
  myRole = response.role;
  gameState = response.state;
  selectedNode = null;
  legalNodes = [];
  movePending = false;
  localNotice = "";
  localStorage.setItem(
    "policeVoleurRoom",
    JSON.stringify({ code: roomCode, role: myRole }),
  );
  showGame();
}

function currentPlayerPieces() {
  return gameState.pieces.filter((piece) => piece.type === myRole);
}

function calculateLegalMoves(piece) {
  const occupied = new Set(
    gameState.pieces.filter((p) => p !== piece).map((p) => p.node),
  );
  return board.neighbors(piece.node).filter((node) => !occupied.has(node));
}

function render() {
  if (!gameState || !myRole) return;

  const players = gameState.playersOnline || { thief: false, police: false };
  const bothConnected = players.thief && players.police;
  const isMyTurn =
    gameState.turn === myRole && !gameState.gameOver && bothConnected;
  const roleName = myRole === "thief" ? "Voleur" : "Policiers";
  const opponentName = myRole === "thief" ? "Policiers" : "Voleur";

  elements.roomCodeDisplay.textContent = roomCode;
  elements.myRole.textContent = roleName;
  elements.myRoleDescription.textContent =
    myRole === "thief"
      ? "Tu contrôles le pion vert."
      : "Tu contrôles les trois pions bleus.";

  if (gameState.gameOver) {
    elements.turnTitle.textContent = "Partie terminée";
    elements.statusText.textContent =
      gameState.winner === myRole
        ? "Bravo, tu as gagné !"
        : "Ton adversaire a gagné cette partie.";
    elements.turnLabel.textContent = "Fin de partie";
    elements.turnDot.style.background = "#e45b61";
  } else if (!bothConnected) {
    elements.turnTitle.textContent = "En attente du joueur";
    elements.statusText.textContent =
      "La partie commencera lorsque les deux joueurs seront connectés.";
    elements.turnLabel.textContent = "Salle incomplète";
    elements.turnDot.style.background = "#d69e35";
  } else {
    const turnName = gameState.turn === "thief" ? "Voleur" : "Policiers";
    elements.turnTitle.textContent = `Tour des ${turnName}`;
    elements.statusText.textContent = isMyTurn
      ? "C’est à toi de jouer. Sélectionne un de tes pions."
      : `Attends le déplacement de ton adversaire (${opponentName.toLowerCase()}).`;
    elements.turnLabel.textContent = isMyTurn
      ? "À toi de jouer"
      : `Adversaire : ${turnName}`;
    elements.turnDot.style.background =
      gameState.turn === "thief" ? "#4caf50" : "#2196f3";
  }

  elements.thiefPresence.classList.toggle("online", Boolean(players.thief));
  elements.policePresence.classList.toggle("online", Boolean(players.police));
  elements.thiefOnline.textContent = players.thief ? "Connecté" : "Absent";
  elements.policeOnline.textContent = players.police ? "Connecté" : "Absent";
  elements.turnCount.textContent = gameState.turns;
  elements.thiefCount.textContent = gameState.thiefMoves;
  elements.message.textContent = localNotice || gameState.message;
  elements.resetGame.disabled = !bothConnected;
  draw();
}

function draw() {
  if (!gameState) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    return;
  }
  board.draw(ctx, gameState.pieces, selectedNode, legalNodes);
}

function handleBoardClick(event) {
  if (!gameState || !myRole || gameState.gameOver || movePending) return;
  const players = gameState.playersOnline || {};
  if (!players.thief || !players.police) {
    localNotice = "La partie attend la connexion des deux joueurs.";
    render();
    return;
  }
  if (gameState.turn !== myRole) {
    localNotice = "Ce n’est pas ton tour. Attends ton adversaire.";
    elements.message.textContent = localNotice;
    return;
  }

  const rect = canvas.getBoundingClientRect();
  const x = ((event.clientX - rect.left) * 720) / rect.width;
  const y = ((event.clientY - rect.top) * 720) / rect.height;
  let nodeId = null;
  let minDistance = 30;
  for (const node of board.nodes) {
    const distance = Math.hypot(node.x - x, node.y - y);
    if (distance < minDistance) {
      minDistance = distance;
      nodeId = node.id;
    }
  }
  if (nodeId === null) return;

  const ownPiece = currentPlayerPieces().find((piece) => piece.node === nodeId);
  if (ownPiece) {
    selectedNode = nodeId;
    legalNodes = calculateLegalMoves(ownPiece);
    localNotice = legalNodes.length
      ? "Choisis une intersection voisine mise en évidence."
      : "Ce pion ne peut pas se déplacer. Sélectionne un autre pion.";
    render();
    return;
  }

  if (selectedNode !== null && legalNodes.includes(nodeId)) {
    movePending = true;
    localNotice = "Déplacement en cours…";
    render();
    socket.emit("move", { from: selectedNode, to: nodeId }, (response) => {
      movePending = false;
      if (!response?.ok) {
        localNotice = response?.error || "Le déplacement a été refusé.";
        render();
      }
    });
    selectedNode = null;
    legalNodes = [];
    return;
  }

  selectedNode = null;
  legalNodes = [];
  localNotice = "Sélectionne un de tes pions.";
  render();
}

function requestRoom(action, payload = {}) {
  if (!socket.connected) {
    elements.lobbyFeedback.textContent =
      "Le serveur est inaccessible. Vérifie que le serveur Node.js est démarré.";
    return;
  }
  elements.createRoom.disabled = true;
  elements.joinRoom.disabled = true;
  elements.lobbyFeedback.textContent = "Connexion à la salle…";
  socket.emit(action, payload, (response) => {
    elements.createRoom.disabled = false;
    elements.joinRoom.disabled = false;
    enterRoom(response);
  });
}

elements.createRoom.addEventListener("click", () => requestRoom("createRoom"));
elements.joinRoom.addEventListener("click", () => {
  const code = elements.roomCodeInput.value.trim().toUpperCase();
  if (!code) {
    elements.lobbyFeedback.textContent = "Entre le code de la salle.";
    return;
  }
  requestRoom("joinRoom", { code });
});
elements.roomCodeInput.addEventListener("input", () => {
  elements.roomCodeInput.value = elements.roomCodeInput.value
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, "");
});
elements.roomCodeInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter") elements.joinRoom.click();
});

canvas.addEventListener("click", handleBoardClick);
elements.resetGame.addEventListener("click", () => {
  if (!roomCode) return;
  socket.emit("resetGame", (response) => {
    if (!response?.ok) {
      localNotice = response?.error || "Impossible de recommencer la partie.";
      render();
    }
  });
});
elements.leaveRoom.addEventListener("click", () => {
  socket.emit("leaveRoom");
  localStorage.removeItem("policeVoleurRoom");
  roomCode = null;
  myRole = null;
  gameState = null;
  selectedNode = null;
  legalNodes = [];
  movePending = false;
  showLobby(
    "Tu as quitté la salle. Tu peux en créer une autre ou en rejoindre une nouvelle.",
  );
});
elements.copyCode.addEventListener("click", async () => {
  if (!roomCode) return;
  try {
    await navigator.clipboard.writeText(roomCode);
    elements.copyCode.textContent = "Copié !";
    setTimeout(() => {
      elements.copyCode.textContent = "Copier";
    }, 1300);
  } catch {
    elements.lobbyFeedback.textContent = `Code de la salle : ${roomCode}`;
  }
});

socket.on("connect", () => {
  setConnection("connected", "Connecté au serveur");
  const savedRoom = localStorage.getItem("policeVoleurRoom");
  if (savedRoom) {
    try {
      const saved = JSON.parse(savedRoom);
      socket.emit("rejoinRoom", saved, (response) => {
        if (response?.ok) enterRoom(response);
        else {
          localStorage.removeItem("policeVoleurRoom");
          showLobby(response?.error || "La reconnexion à la salle a échoué.");
        }
      });
    } catch {
      localStorage.removeItem("policeVoleurRoom");
    }
  }
});

socket.on("disconnect", () => {
  setConnection("disconnected", "Serveur déconnecté");
  if (gameState) {
    localNotice = "Connexion perdue. Reconnexion automatique en cours…";
    elements.message.textContent = localNotice;
  } else {
    elements.lobbyFeedback.textContent =
      "Connexion au serveur perdue. Vérifie le réseau local.";
  }
});

socket.on("gameUpdate", (payload) => {
  if (!roomCode || payload.code !== roomCode) return;
  gameState = payload.state;
  selectedNode = null;
  legalNodes = [];
  movePending = false;
  localNotice = "";
  render();
});

socket.on("connect_error", () => {
  setConnection("disconnected", "Serveur inaccessible");
  elements.lobbyFeedback.textContent =
    "Impossible de joindre le serveur. Vérifie son adresse et le réseau Wi-Fi.";
});

window.addEventListener("resize", draw);

setConnection("", "Connexion au serveur…");
