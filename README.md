# **EusRECALL**
## · Introduction
>Do you sometimes PVP or hold an activity in Minecraft? But it is a little difficult to teleport your players to your location. **EusRECALL** has solved this problem.

>**Server** : Bukkit / Spigot / PaperSpigot 1.15.X **[Continually Updated]**

>**Depends** : Vault
## · Features
>1. Teleport Players to a fixed location.
>2. Players can accept your request by themselves.
>3. You can cancel your request when it doesn't end.
>4. You can customize some parameters. <a href="https://github.com/ElaBosak233/EusRECALL/blob/master/src/main/resources/config.yml" target="_blank">(config.yml)</a>
>5. **EusRECALL** uses Apache2.0 License to open source.
## · Command
>* /rc --- The main command in **EusRECALL** , you can call your players in this way . **But** it will cost your money , also it will set up a timer , when time is up , request will be closed , you can customize them in **"Config.yml"**.By the way , don't type this command **too many times** , server won't let it start !

>**Timer Example**
```java
Bukkit.getScheduler().runTaskAsynchronously(this , new Runnable() {
       @Override
       public void run() {
       sender.sendMessage("§d§l[[RECALL]]: Starting the timer...");
       try {
            Thread.sleep(getConfig().getInt("RECALL.duration"));
            //RECALL.duration form config.yml
           } catch (InterruptedException e) {
           //Null
}
```

>![rc.PNG](https://i.loli.net/2020/02/09/zjOkYbuRN2rSBIg.png)
>![Timeout.PNG](https://i.loli.net/2020/02/09/TQhH9zCWZ7Jibam.png)
>![too many times.PNG](https://i.loli.net/2020/02/09/jZLq41wzGBVrdux.png)
>* /rcaccept [PlayerName] --- Accept a player's "RECALL" request . **But** , please remember the **[PlayerName]**

>![rcaccept-pn.PNG](https://i.loli.net/2020/02/09/fHv2Eb6JaMGmSzP.png)
>![rcaccept.PNG](https://i.loli.net/2020/02/09/f7zoCB1cJmZTePq.png)
>* /rccancel --- Cancel your request

![rccancel.PNG](https://i.loli.net/2020/02/09/EIRj2oSFWfGdB1b.png)
>* /rchelp --- Get **EusRECALL** help

>![rchelp.PNG](https://i.loli.net/2020/02/09/LvSr4uAmEVTo8xF.png)
## · Console
>Every Commands can not be used in server console , but it will only write a log .

>![server-start.PNG](https://i.loli.net/2020/02/09/nLo7brvjIUw9f6d.png)

## · Issues
>If you find a bug in this plugin or make suggestions for this plugin, please feedback in Issues, thank you !

## · Collaborators
> 1. Leaves123
> 2. ElaBosak233

## · License
>Apache 2.0 License

>https://github.com/ElaBosak233/EusRECALL


