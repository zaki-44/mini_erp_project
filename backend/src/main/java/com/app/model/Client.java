package com.app.model;

import com.app.model.Enums.Role;

public class Client extends User {
    private String address;

    public Client() {}

    public Client(int id, String email, String username, String passwordHash,
                  String firstName, String lastName, String phoneNumber, Role role,
                  String address, boolean active) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber, role);
        this.address = address;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    @Override
    public void print(){
        super.print();
        System.out.println("Client [address=" + address + "]");
    }
}
