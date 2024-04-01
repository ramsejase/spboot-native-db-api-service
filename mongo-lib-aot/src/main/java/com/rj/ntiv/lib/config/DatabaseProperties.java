package com.rj.ntiv.lib.config;

public class DatabaseProperties {
        String name;
        String host;
        String port;
        String db;
        String user;
        String secret;

        public void setHost(String host) {
            this.host = host;
        }

        public String getHost() {
            return host;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getPort() {
            return port;
        }

        public void setDb(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }
        
        public void setUser(String user) {
            this.user = user;
        }

        public String getUser() {
            return user;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getSecret() {
            return secret;
        }        
}