package com.marco.constant;

import cn.bmob.v3.BmobObject;

public class RealTimeData extends BmobObject{
        private String Username;
        private String Data;
		public RealTimeData(String name, String msg) {
			this.Username=name;
			this.Data=msg;
		}
		public String getUsername() {
			return Username;
		}
		public void setUsername(String username) {
			Username = username;
		}
		public String getData() {
			return Data;
		}
		public void setData(String data) {
			Data = data;
		}
}
