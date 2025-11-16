package com.example.shopmohinh;
import io.github.cdimascio.dotenv.Dotenv;
import com.example.shopmohinh.configuration.EnvConfig;

public class TestEnv {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String momo_api0 = EnvConfig.get("MOMO_ACCESS_KEY");
        String momo_api1 = dotenv.get("MOMO_ACCESS_KEY");
        String momo_api2 = System.getenv("MOMO_ACCESS_KEY");
        System.out.println("api: " + momo_api0 + " " + momo_api1 + " " + momo_api2);
    }
}
