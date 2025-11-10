package com.example.kairos;

import java.util.Arrays;
import java.util.List;

public final class CountryCityData_Oceania implements CountryCityData {
    @Override public List<Country> countries() { return Arrays.asList(
            new Country("AU","Australia", Arrays.asList("Sydney","Melbourne","Brisbane","Perth","Adelaide","Gold Coast","Cairns","Hobart")),
            new Country("NZ","New Zealand", Arrays.asList("Auckland","Wellington","Queenstown","Christchurch","Rotorua","Dunedin")),
            new Country("PG","Papua New Guinea", Arrays.asList("Port Moresby","Lae","Madang","Mount Hagen","Kokopo")),
            new Country("FJ","Fiji", Arrays.asList("Suva","Nadi","Lautoka","Sigatoka","Savusavu")),
            new Country("SB","Solomon Islands", Arrays.asList("Honiara","Auki","Gizo","Kirakira","Munda")),
            new Country("VU","Vanuatu", Arrays.asList("Port Vila","Luganville","Lenakel","Saratamata","Sola")),
            new Country("WS","Samoa", Arrays.asList("Apia","Vaitele","Faleula","Siusega","Salelologa")),
            new Country("TO","Tonga", Arrays.asList("Nuku'alofa","Neiafu","Pangai","ʻOhonua","Haveluloto")),
            new Country("FM","Micronesia", Arrays.asList("Palikir","Weno","Kolonia","Tofol","Colonia")),
            new Country("MH","Marshall Islands", Arrays.asList("Majuro","Ebeye","Arno","Jabor","Jaluit")),
            new Country("PW","Palau", Arrays.asList("Ngerulmud","Koror","Airai","Melekeok","Ngchesar")),
            new Country("KI","Kiribati", Arrays.asList("South Tarawa","Betio","Bikenibeu","Bairiki","Kiritimati")),
            new Country("NR","Nauru", Arrays.asList("Yaren","Aiwo","Anabar","Anibare","Ijuw")),
            new Country("TV","Tuvalu", Arrays.asList("Funafuti","Fongafale","Savave","Tafua","Tanrake"))
    );}
}
