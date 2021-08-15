SyncHelper
===================================

## Introduction
Application for keeping Android and Windows notifications synchronized based on [NotificationListenerService-Example](https://github.com/kpbird/NotificationListenerService-Example/tree/master/NLSExample/src/main) and Socket communication

## Use
* Download and install [SyncHelper-releases.apk](https://github.com/TouwaErioer/SyncHelper/releases) Android Application
* Download and run [SyncHelper-server.exe](https://github.com/TouwaErioer/SyncHelper/releases) Server on Windows
* Open cmd, type `ipconfig`, write down the ipv4 address of your network card, go to the `SyncHelper`, click the `Add` button, enter the ipv4 address of your network card in the input box, click `Save` and go back to the home page, click the `Connect` button to connect to your computer
* The computer allows the device you credit to connect
* Click the `test` button to test the function properly

## Note
* The file is saved in the same directory as SyncHelper-server.exe

## Useful Methods
1. Notification
	* Implement Android and Windows notification synchronization in the same network
2. Cross-platform clipboard
	* Implementing Android to send text to Windows in the same network
3. File transfer
    * Implement Android to send files to Windows in the same network
4. Server authentication
    * Only allowed devices can connect to this computer
5. Efficient Socket Connection
    * Reuse the same socket connection whenever possible

## Thanks

Thanks to the [NotificationListenerService-Example](https://github.com/kpbird/NotificationListenerService-Example/tree/master/NLSExample/src/main) project for basic support

## Screenshots
![server](https://s3.jpg.cm/2021/08/15/IcSi9h.png)
![client](https://s3.jpg.cm/2021/08/15/IcYI1L.png)