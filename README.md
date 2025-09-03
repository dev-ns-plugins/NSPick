## :package: NSPick

**NSPick** é um plugin de mineração avançada para servidores Minecraft. Ele oferece uma picareta personalizada com quebra em área 3x3, encantamentos configuráveis, entrega via comando e suporte à evolução. Ideal para servidores que buscam eficiência, estilo e jogabilidade dinâmica.

---

## :white_check_mark: Requisitos

- **Minecraft Version:** 1.8.8+
- **Server Software:** Spigot, Paper ou forks compatíveis
- **Java Version:** 8+

---

## :gear: Instalação

1. Baixe a versão mais recente do plugin [aqui](https://github.com/dev-ns-plugins/NSPick)
2. Coloque o arquivo `.jar` na pasta `plugins/` do seu servidor
3. Inicie o servidor para gerar os arquivos de configuração
4. Edite o `config.yml` conforme suas preferências

---

## :speech_balloon: Comandos

| Comando | Permissão | Descrição |
|--------|-----------|-----------|
| `/nspick` | `nspick.give` | Dá a picareta ao jogador que executa |
| `/nspick give <jogador>` | `nspick.give` | Dá a picareta para outro jogador |

---

## :closed_lock_with_key: Permissões

| Permissão | Padrão | Descrição |
|-----------|--------|-----------|
| `nspick.give` | OP | Permite dar a picareta personalizada |

---

## :tools: Configuração

```yaml
pickaxe:
  name: "&8&lNSPICKAXE V.1"
  lore:
    - "&8&lMINERAÇÃO EM ÁREA: &73X3"
    - ""
    - "&edsc.gg/nsplugins"
  material: "DIAMOND_PICKAXE"
  enchantments:
    dig_speed: 5
    loot_bonus_blocks: 3
  unbreakable: true
