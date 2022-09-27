/*
 * Copyright 2021 WaterdogTEAM
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.waterdog.waterdogpe.utils;

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfoMap;
import dev.waterdog.waterdogpe.utils.config.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ConfigurationManager {

    private final ProxyServer proxy;
    private ProxyConfig proxyConfig;
    private LangConfig langConfig;
    private CredentialsConfig credentialsConfig;

    public ConfigurationManager(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Deprecated
    public static Configuration newConfig(File file, Type type) {
        return newConfig(file.toString(), type);
    }

    @Deprecated
    public static Configuration newConfig(String file, Type type) {
        switch (type) {
            case YAML:
                return new YamlConfig(file);
            case JSON:
                return new JsonConfig(file);
            default:
                return null;
        }
    }

    public void loadProxyConfig() throws InvalidConfigurationException {
        File configFile = new File(this.proxy.getDataPath().toString() + "/config.yml");
        ProxyConfig config = new ProxyConfig(configFile);
        config.init();
        this.proxyConfig = config;
    }

    public void loadServerInfos(ServerInfoMap serverInfoMap) {
        for (ServerEntry entry : this.proxyConfig.getServerList().values()) {
            try {
                serverInfoMap.putIfAbsent(entry.getServerName(), serverInfoMap.fromServerEntry(entry));
            } catch (Exception e) {
                this.proxy.getLogger().error("Failed to create ServerInfo from "+entry, e);
            }
        }
    }

    public void loadLanguage() {
        File langFile = new File(this.proxy.getDataPath().toString() + "/lang.ini");
        if (!langFile.exists()) {
            try {
                FileUtils.saveFromResources("lang.ini", langFile);
            } catch (IOException e) {
                this.proxy.getLogger().error("Can not save lang file!", e);
            }
        }
        this.langConfig = new LangConfig(langFile);
    }

    public void loadCredentials(){
        File credentialsFile = new File(this.proxy.getDataPath().toString() + "/credentials.yml");
        CredentialsConfig credentialsConfig = new CredentialsConfig(credentialsFile);
        credentialsConfig.init();
        this.credentialsConfig = credentialsConfig;
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    public ProxyConfig getProxyConfig() {
        return this.proxyConfig;
    }

    public LangConfig getLangConfig() {
        return this.langConfig;
    }

    public CredentialsConfig getCredentialsConfig() {
        return this.credentialsConfig;
    }

    @AllArgsConstructor
    public enum Type {
        JSON(1),
        YAML(2),
        UNKNOWN(-1);

        @Getter
        private final int id;

        public static Type getTypeById(int id) {
            return Arrays.stream(Type.values()).filter(type -> type.getId() == id).findFirst().orElse(Type.UNKNOWN);
        }
    }
}
