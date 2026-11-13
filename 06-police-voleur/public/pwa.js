'use strict';

(() => {
  const installButton = document.getElementById('installApp');
  if (!installButton) return;

  let installPrompt = null;
  const isStandalone = window.matchMedia('(display-mode: standalone)').matches
    || window.navigator.standalone === true;
  const isIOS = /iphone|ipad|ipod/i.test(window.navigator.userAgent);
  const isSecureOrigin = window.isSecureContext;

  if (isStandalone) {
    installButton.classList.add('hidden');
  }

  if ('serviceWorker' in navigator && isSecureOrigin) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js').catch(error => {
        console.warn('Enregistrement du Service Worker impossible :', error);
      });
    });
  }

  window.addEventListener('beforeinstallprompt', event => {
    event.preventDefault();
    installPrompt = event;
    installButton.classList.remove('hidden');
  });

  installButton.addEventListener('click', async () => {
    if (installPrompt) {
      installPrompt.prompt();
      const choice = await installPrompt.userChoice;
      if (choice.outcome === 'accepted') installButton.classList.add('hidden');
      installPrompt = null;
      return;
    }

    if (isIOS) {
      window.alert('Pour installer le jeu sur iPhone/iPad : touche le bouton Partager, puis « Sur l’écran d’accueil ».');
      return;
    }

    if (!isSecureOrigin) {
      window.alert('Pour installer cette PWA, ouvre le jeu avec une adresse HTTPS. Sur le réseau local, une adresse http://192.168.x.x ne permet pas au navigateur d’activer le Service Worker.');
      return;
    }

    window.alert('Si ton navigateur propose l’installation, ouvre son menu puis choisis « Installer l’application » ou « Ajouter à l’écran d’accueil ».');
  });

  window.addEventListener('appinstalled', () => {
    installButton.classList.add('hidden');
    installPrompt = null;
  });
})();
