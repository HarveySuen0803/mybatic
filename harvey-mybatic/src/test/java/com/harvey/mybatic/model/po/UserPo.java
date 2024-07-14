package com.harvey.mybatic.model.po;

import java.io.Serial;
import java.io.Serializable;

public class UserPo implements Serializable {
    private Long id;
    
    private String username;
    
    private String password;
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "UserPo{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            '}';
    }
}
