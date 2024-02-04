package com.tony;

import java.util.Scanner;

import com.tony.models.User;

public class App {
    public static void main(String[] args) {
        String name;
        Scanner sc = new Scanner(System.in);
        User.createModel();
        
        System.out.println("Hello");
        System.out.print("What's your name: ");
        name = sc.nextLine();

        try{
            User user = new User(name);
            user.save();
            System.out.println("Success");
        }catch(Exception e){
            e.printStackTrace();
        }

        sc.close();
    }
}
