package com.example.kairos;

import java.util.Arrays;
import java.util.List;

public final class CountryCityData_Asia implements CountryCityData {
    @Override public List<Country> countries() { return Arrays.asList(
            new Country("AF","Afghanistan", Arrays.asList("Kabul","Herat","Mazar-i-Sharif","Kandahar","Bamyan")),
            new Country("AM","Armenia", Arrays.asList("Yerevan","Gyumri","Dilijan","Vanadzor","Sevan")),
            new Country("AZ","Azerbaijan", Arrays.asList("Baku","Ganja","Sumqayit","Sheki","Quba")),
            new Country("BH","Bahrain", Arrays.asList("Manama","Muharraq","Isa Town","Riffa","Budaiya")),
            new Country("BD","Bangladesh", Arrays.asList("Dhaka","Chittagong","Sylhet","Rajshahi","Cox's Bazar")),
            new Country("BT","Bhutan", Arrays.asList("Thimphu","Paro","Punakha","Phuentsholing","Jakar")),
            new Country("BN","Brunei", Arrays.asList("Bandar Seri Begawan","Kuala Belait","Seria","Tutong","Jerudong")),
            new Country("KH","Cambodia", Arrays.asList("Phnom Penh","Siem Reap","Sihanoukville","Battambang","Kampot")),
            new Country("CN","China", Arrays.asList("Beijing","Shanghai","Xi'an","Guangzhou","Shenzhen","Chengdu","Hangzhou","Guilin")),
            new Country("GE","Georgia", Arrays.asList("Tbilisi","Batumi","Kutaisi","Mestia","Telavi")),
            new Country("IN","India", Arrays.asList("Delhi","Mumbai","Bengaluru","Jaipur","Agra","Chennai","Hyderabad","Kolkata")),
            new Country("ID","Indonesia", Arrays.asList("Jakarta","Bali","Yogyakarta","Bandung","Surabaya","Ubud")),
            new Country("IR","Iran", Arrays.asList("Tehran","Isfahan","Shiraz","Mashhad","Tabriz")),
            new Country("IQ","Iraq", Arrays.asList("Baghdad","Erbil","Basra","Najaf","Karbala")),
            new Country("IL","Israel", Arrays.asList("Jerusalem","Tel Aviv","Haifa","Eilat","Nazareth")),
            new Country("JP","Japan", Arrays.asList("Tokyo","Kyoto","Osaka","Hiroshima","Sapporo","Nara","Fukuoka","Nagoya")),
            new Country("JO","Jordan", Arrays.asList("Amman","Petra","Aqaba","Jerash","Madaba")),
            new Country("KZ","Kazakhstan", Arrays.asList("Almaty","Astana","Shymkent","Aktau","Karaganda")),
            new Country("KW","Kuwait", Arrays.asList("Kuwait City","Salmiya","Hawally","Fahaheel","Jahra")),
            new Country("KG","Kyrgyzstan", Arrays.asList("Bishkek","Osh","Karakol","Cholpon-Ata","Naryn")),
            new Country("LA","Laos", Arrays.asList("Vientiane","Luang Prabang","Pakse","Vang Vieng","Savannakhet")),
            new Country("LB","Lebanon", Arrays.asList("Beirut","Byblos","Tripoli","Sidon","Baalbek")),
            new Country("MY","Malaysia", Arrays.asList("Kuala Lumpur","George Town","Kota Kinabalu","Malacca","Johor Bahru")),
            new Country("MV","Maldives", Arrays.asList("Malé","Addu City","Hulhumalé","Maafushi","Fuvahmulah")),
            new Country("MN","Mongolia", Arrays.asList("Ulaanbaatar","Erdenet","Darkhan","Dalanzadgad","Kharkhorin")),
            new Country("MM","Myanmar", Arrays.asList("Yangon","Mandalay","Naypyidaw","Bagan","Inle Lake")),
            new Country("NP","Nepal", Arrays.asList("Kathmandu","Pokhara","Bhaktapur","Lalitpur","Chitwan")),
            new Country("KP","North Korea", Arrays.asList("Pyongyang","Hamhung","Wonsan","Nampo","Kaesong")),
            new Country("OM","Oman", Arrays.asList("Muscat","Nizwa","Salalah","Sur","Sohar")),
            new Country("PK","Pakistan", Arrays.asList("Karachi","Lahore","Islamabad","Peshawar","Multan")),
            new Country("PS","Palestine", Arrays.asList("Ramallah","Bethlehem","Hebron","Jericho","Nablus")),
            new Country("PH","Philippines", Arrays.asList("Manila","Cebu","Davao","Boracay","Baguio")),
            new Country("QA","Qatar", Arrays.asList("Doha","Al Wakrah","Al Khor","Lusail","Al Rayyan")),
            new Country("SA","Saudi Arabia", Arrays.asList("Riyadh","Jeddah","Mecca","Medina","Dammam")),
            new Country("SG","Singapore", Arrays.asList("Singapore")),
            new Country("KR","South Korea", Arrays.asList("Seoul","Busan","Jeju City","Incheon","Daegu")),
            new Country("LK","Sri Lanka", Arrays.asList("Colombo","Kandy","Galle","Nuwara Eliya","Jaffna")),
            new Country("SY","Syria", Arrays.asList("Damascus","Aleppo","Homs","Latakia","Tartus")),
            new Country("TW","Taiwan", Arrays.asList("Taipei","Taichung","Tainan","Kaohsiung","Hualien")),
            new Country("TJ","Tajikistan", Arrays.asList("Dushanbe","Khujand","Khorugh","Istaravshan","Panjakent")),
            new Country("TH","Thailand", Arrays.asList("Bangkok","Chiang Mai","Phuket","Pattaya","Krabi","Ayutthaya")),
            new Country("TL","Timor-Leste", Arrays.asList("Dili","Baucau","Same","Suai","Viqueque")),
            new Country("TM","Turkmenistan", Arrays.asList("Ashgabat","Mary","Turkmenabat","Dashoguz","Awaza")),
            new Country("AE","United Arab Emirates", Arrays.asList("Dubai","Abu Dhabi","Sharjah","Al Ain","Ras Al Khaimah")),
            new Country("UZ","Uzbekistan", Arrays.asList("Tashkent","Samarkand","Bukhara","Khiva","Fergana")),
            new Country("VN","Vietnam", Arrays.asList("Hanoi","Ho Chi Minh City","Da Nang","Hoi An","Nha Trang","Hue")),
            new Country("YE","Yemen", Arrays.asList("Sana'a","Aden","Taiz","Ibb","Mukalla"))
            // (TR/Türkiye et RU/Russia sont déjà dans l’Europe pour éviter les doublons)
    );}
}
