package Entities;

public class Account {
    String fName,
            lName,
            email,
            password,
            userName,
            lastLoginTime,
            createDate;

    public Account(String fName, String lName, String email, String password, String userName, String lastLoginTime, String createDate){
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.createDate = createDate;
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public String toString() {
        return "Account{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", lastLoginTime=" + lastLoginTime +
                ", createDate=" + createDate +
                '}';
    }
}
