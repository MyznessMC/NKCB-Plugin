package yuziouo.teams;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.ItemID;
import cn.nukkit.plugin.PluginBase;
import yuziouo.teams.cmds.TeamCmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Loader extends PluginBase implements Listener {
    public static HashMap<String, String> req = new HashMap<>();
    static final int f = 98378734;
    static final int all = 98378735;
    static final int cr = 98378736;
    static final int info = cr+1;
    static final int pinfo = info+1;
    static final int quite = pinfo+1;
    static final int invite = quite+1;
    static final int kick = invite+1;
    public static Loader instance;
    Team team;
    @Override
    public void onEnable() {
        instance = this;
        team = new Team();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getCommandMap().register(getConfig().getString("指令"),new TeamCmd());
        this.getLogger().info("插件已开启，原作者:yuziouo，简体化:眠悠子Myzness");
        this.getLogger().info("小广告:加入Nukkit中文社区，支持一下发展:bbs.nukkit.cc（非NK官方社区）");
        this.getLogger().info("该插件如果有后续更新，首发在Nukkit中文社区哦");
        team.load();
        getServer().getScheduler().scheduleRepeatingTask(this,new CleanEmptyTeam(),20);
    }

    @Override
    public void onLoad() {
        this.getLogger().info("插件加载中，请耐心等待");
    }

    @Override
    public void onDisable() {
        team.save();
        this.getLogger().info("插件已卸载，感谢使用");
    }
    public  void sendUI(Player player){
        FormWindowSimple form = new FormWindowSimple(getConfig().getString("未加入队伍系统表单标题"),"");
        form.addButton(new ElementButton(getConfig().getString("未加入队伍系统表单查看可加入队伍按钮")));
        form.addButton(new ElementButton(getConfig().getString("未加入队伍系统表单创建新队伍按钮")));
        player.showFormWindow(form,f);
    }
    public void createUi(Player player){
        FormWindowCustom form = new FormWindowCustom(getConfig().getString("创建队伍表单标题"));
        form.addElement(new ElementInput(getConfig().getString("创建队伍表单输入提示")));
        player.showFormWindow(form,cr);
    }
    public void AllTeamUi(Player player){
        FormWindowSimple form = new FormWindowSimple(getConfig().getString("服务器所有可加入队伍表单标题"),getConfig().getString("服务器所有可加入队伍表单提示"));
        for (String key : Team.list.keySet()){
            if (team.getTeamSize(key)>=5)
                continue;
            form.addButton(new ElementButton(key));
        }
        player.showFormWindow(form,all);
    }
    public  void quiteUI(Player player){
        FormWindowSimple form = new FormWindowSimple(getConfig().getString("退出队伍表单标题"),"");
        form.addButton(new ElementButton(getConfig().getString("退出队伍表单退出按钮")));
        form.addButton(new ElementButton("返回"));
        player.showFormWindow(form,quite);
    }
    public void TeamInfo(Player player){
        FormWindowSimple formWindowSimple = new FormWindowSimple(getConfig().getString("队伍系统表单标题"),"");
        formWindowSimple.addButton(new ElementButton(getConfig().getString("队伍系统查看队伍人数按钮")));
        formWindowSimple.addButton(new ElementButton(getConfig().getString("队伍系统退出队伍按钮")));
        if (team.isTeamLeader(player)){
            formWindowSimple.addButton(new ElementButton(getConfig().getString("队长查看队伍邀请按钮")));
            formWindowSimple.addButton(new ElementButton(getConfig().getString("队长踢出成员按钮")));
        }
        player.showFormWindow(formWindowSimple,info);
    }
    public void playerteaminfo(Player player){
        String old = getConfig().getString("成员名单"), young = old.replace("@a",team.getTeamName(player));
        FormWindowSimple formWindowSimple = new FormWindowSimple(young,"");
        for (int i=0; i<team.getTeam(player).size();i++){
            formWindowSimple.addButton(new ElementButton(team.getTeam(player).get(i)));
        }
        player.showFormWindow(formWindowSimple,pinfo);
    }
    public void inviteUi(Player player){
        FormWindowSimple formWindowSimple = new FormWindowSimple(getConfig().getString("队长邀请函标题"),"点击按钮同意");
        for (Map.Entry<String, String> entry : req.entrySet()) {
            if (!entry.getValue().equals(team.getTeamName(player)))
                continue;
            formWindowSimple.addButton(new ElementButton(entry.getKey()));
        }
        player.showFormWindow(formWindowSimple,invite);
    }
    public void kickUi(Player player){
        FormWindowSimple formWindowSimple = new FormWindowSimple(getConfig().getString("队长踢出玩家表单标题"),"点击按钮踢出玩家");
        for (int i=0; i<team.getTeam(player).size();i++){
            //排除隊長
            if (player.getName().equals(team.getTeam(player).get(i)))
                continue;
            formWindowSimple.addButton(new ElementButton(team.getTeam(player).get(i)));
        }
        player.showFormWindow(formWindowSimple,kick);
    }
    @EventHandler
    public void onForm(PlayerFormRespondedEvent event){
        Player player = event.getPlayer();
        int id = event.getFormID(); //这将返回一个form的唯一标识`id`
        if (event.wasClosed())
            return;
        if(id == f) { //判断出这个UI界面是否是我们上面写的`menu`
            FormResponseSimple response = (FormResponseSimple) event.getResponse(); //这里需要强制类型转换一下
            int clickedButtonId = response.getClickedButtonId();
            if (event.wasClosed())
                return;
            if (clickedButtonId == 0) {
                AllTeamUi(player);
            }else if (clickedButtonId ==1){
                createUi(player);
            }
        }
        if (id == all){
            FormResponseSimple response = (FormResponseSimple) event.getResponse(); //这里需要强制类型转换一下
            if (event.wasClosed())
                return;
            req.put(player.getName(),response.getClickedButton().getText());
            String old = getConfig().getString("玩家发送申请给队伍") ,young = old.replace("@a",response.getClickedButton().getText());
            player.sendMessage(young);
            old = getConfig().getString("队长收到申请消息通知");
            young = old.replace("@p",player.getName());
            Server.getInstance().getPlayer(team.getTeamLeader(response.getClickedButton().getText())).sendMessage(young);
        }
        if (id == cr){
            FormResponseCustom response = (FormResponseCustom) event.getResponse();
            if (event.wasClosed())
                return;
            if (response.getInputResponse(0).equals("")){
                player.sendMessage("队伍名称不能为空白");
                return;
            }
            team.createTeam(response.getInputResponse(0),player);
            if (req.containsKey(player.getName()))
                req.remove(player.getName());
                player.sendMessage("队伍创建成功");
        }
        if (id == info){
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            if (event.wasClosed())
                return;
            int clickedButtonId = response.getClickedButtonId();
            switch (clickedButtonId){
                case 0:
                    playerteaminfo(player);
                    break;
                case 1:
                    quiteUI(player);
                    break;
                case 2:
                    inviteUi(player);
                    break;
                case 3:
                    kickUi(player);
            }
        }
        if (id == pinfo){
            return;
        }
        if (id == invite){
            if (event.wasClosed())
                return;
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            team.joinTeam(team.getTeamName(player), Server.getInstance().getPlayer(response.getClickedButton().getText()));
            String old =getConfig().getString("加入队伍玩家发送消息"),young = old.replace("@a",team.getTeamName(player));
            Server.getInstance().getPlayer(response.getClickedButton().getText()).sendMessage(young);
            for (int i = 0; i<team.getTeam(player).size();i++) {
                Player player1 = Server.getInstance().getPlayer(team.getTeam(player).get(i));
                if (player1 != null) {
                    if (!player1.getName().equals(player.getName())) {
                        old = getConfig().getString("其余玩家发送加入消息");
                        young = old.replace("@p", Server.getInstance().getPlayer(response.getClickedButton().getText()).getName());
                        player1.sendMessage(young);
                    }
                }
            }
            req.remove(Server.getInstance().getPlayer(response.getClickedButton().getText()).getName());
        }
        if (id == quite){
            if (event.wasClosed())
                return;
            for (int i = 0; i<team.getTeam(player).size();i++) {
                Player player1 = Server.getInstance().getPlayer(team.getTeam(player).get(i));
                if (player1!=null) {
                    if (!player1.getName().equals(player.getName())) {
                        player1.sendMessage(getConfig().getString("退出队伍消息").replace("@p", player.getName()).replace("@a", team.getTeamName(player1)));
                    }
                }
            }
            team.quiteTeam(player);
            player.sendMessage(getConfig().getString("退出队伍玩家发送消息"));
        }
        if (id == kick) {
            if (event.wasClosed())
                return;
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            team.quiteTeam(Server.getInstance().getPlayer(response.getClickedButton().getText()));
        }
    }

    public static Loader getInstance() {
        return instance;
    }
}
