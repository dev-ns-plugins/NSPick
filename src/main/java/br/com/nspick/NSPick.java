package br.com.nspick;

// Importações necessárias para comandos, eventos e manipulação de jogadores
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NSPick extends JavaPlugin implements Listener {

    // Gerenciador da picareta personalizada
    private PickaxeManager pickaxeManager;

    // Método chamado quando o plugin é ativado
    @Override
    public void onEnable() {
        // Cria o config.yml padrão se não existir
        saveDefaultConfig();

        // Inicializa o gerenciador da picareta e carrega os valores do config.yml
        pickaxeManager = new PickaxeManager(this);
        pickaxeManager.loadConfigValues();

        // Registra o comando /nspick
        PluginCommand command = getCommand("nspick");
        if (command != null) {
            command.setExecutor(new CommandExecutor() {
                @Override
                public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

                    // Comando básico: /nspick → dá a picareta ao próprio jogador
                    if (args.length == 0 && sender instanceof Player) {
                        Player player = (Player) sender;

                        // Verifica permissão
                        if (!player.hasPermission("nspick.give")) {
                            player.sendMessage("§cVocê não tem permissão.");
                            return true;
                        }

                        // Dá a picareta e envia mensagem de confirmação
                        pickaxeManager.givePickaxe(player);
                        player.sendMessage("§aVocê recebeu a §f" + ChatColor.stripColor(pickaxeManager.getPickaxeName()));
                        return true;
                    }

                    // Comando avançado: /nspick give <jogador> → envia a picareta para outro jogador
                    if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                        // Verifica se é operador ou console
                        if (!(sender.hasPermission("nspick.give") || sender instanceof ConsoleCommandSender)) {
                            sender.sendMessage("§cSem permissão.");
                            return true;
                        }

                        // Busca o jogador alvo
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null || !target.isOnline()) {
                            sender.sendMessage("§cJogador não encontrado.");
                            return true;
                        }

                        // Dá a picareta ao jogador alvo
                        pickaxeManager.givePickaxe(target);
                        sender.sendMessage("§aEnviado para §f" + target.getName());
                        target.sendMessage("§aVocê recebeu a " + ChatColor.stripColor(pickaxeManager.getPickaxeName()));
                        return true;
                    }

                    // Mensagem de ajuda para uso incorreto
                    sender.sendMessage("§cUso: /nspick ou /nspick give <jogador>");
                    return true;
                }
            });
        }

        // Registra os eventos da classe (ex: quebra de blocos)
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // Evento chamado quando um bloco é quebrado
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        // Verifica se o item é a picareta do NSPick
        if (!pickaxeManager.isNSPick(item)) return;

        // Executa a mineração em área
        pickaxeManager.breakArea(event.getBlock(), item, player);
    }
}
