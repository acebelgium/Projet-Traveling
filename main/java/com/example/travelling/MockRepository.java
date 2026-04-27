package com.example.travelling;

import java.util.ArrayList;
import java.util.List;

public class MockRepository {

    public static List<TravelPhoto> getFeedPhotos() {
        List<TravelPhoto> photos = new ArrayList<>();
        photos.add(new TravelPhoto("1", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuB4An4lKl_FA5fihzmfsSOG8Ac0Fy2byxa6_Sl4dVsXeAond0sNA_Lm6BK6g45fV6uBq3USUHXwE06h3X_7Id_6VReje_JQYk544lZz7q0O2J1VCGCwBeQ0nA-j1-j0oKBVLfQJ9qv8oyIvCk7b_tGaYe_8otDwj850y2YmV5QdkZ4LDN_OJ3eKHuStGCDvUPXPHMuon_MYnhh5f1ucp7C_IExNxx1B3j_CSSKw9B-Voyz7SBuXunVzgUxr5l3t-lDL2sr2KHk6SNBM", 
            "Vallée de Yosemite", "1.2k",
            "L'immensité de la nature", "Un réveil brumeux au coeur des montagnes. L'air pur et le silence absolu de la vallée."));
        
        photos.add(new TravelPhoto("2", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuC_HlGQo65Uw5XsMmJ2F7AsHGHNCfdgh2QTYW2NjW0L2rKqaeigb6vsCOYKMwqcFsidYLTmng29M8FhIrLOsmigUnR59_oQ-dRCsnfJnv5fGFJM5sOOWFw9Tp2rzbgECPrF2TGhZL6WhVOL8labvF6faRxhnC7HyFNGiASUxcb_s2UV-S4DCzML_PL-eIoj7SBKul0PD1ZxJDPgYrg2lSBfaLHGemHvGXo5t6JnD6KcAqwTmw6KMpsGPP4LstFZ2c9kSix2hvMw9u3D", 
            "Bassin de l'Amazone", "856",
            "L'enfer vert", "Une exploration en pirogue à travers la forêt tropicale. Chaque son est une nouvelle découverte."));
        
        photos.add(new TravelPhoto("3", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuD-Dz3RlCQXHQAphIX60YCNGMk_YirGU3P_CXUjsmvLbHx2Duk8VuLPSp8dgcS6bbBFrSHTrbt3IN-CHhAOw4xGegsW69BwKCZNH-__zBhMhD_weczVYE_I2MIo3sDtmkVo6btjewzlrNcFMF5O8ghPwn5eR_gOOC55vnCQon59NHJZ-UpQzvCmX1tEFMTQEvYibuvw3VfAn1s7kgzk0FS4L71NyFHacHdKzJRLm4rmfSeLnTmJdYoDqcsbsJhDydgg9bNtyv3j41rX", 
            "Parc national de Banff", "2.4k",
            "Reflets turquoise", "Les eaux cristallines du lac Moraine. Un paysage qui semble irréel tellement les couleurs sont vives."));
        
        photos.add(new TravelPhoto("4", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCKEWo-o27n3vthw-tEIj2SlTeYPcn62Emg6qN-RAJF5o22SEzo9ZIa2cPE7efq2QiRtB-3KyaTrOE6Hjqm-KQark6DJIAQ6C7DhpE1PjzuXRg6egacSjbjRaEBrwnpHSRc1Hvp0C6SjqmduSJ9Adi4GwfL6iseAIz7cfcWDEPr502nfevZLiYVFLLC8ia-V4EXFvu__5Vodwb9xiv5Je0X-fJd6ZUDcGu1E5j7-9SFU9oIMs4VUK-B8dxPuzSTQIvDcFMwSnvRTs5S", 
            "Forêt de Redwoods", "3.1k",
            "Les géants de bois", "Se sentir tout petit face aux arbres les plus hauts du monde. Une cathédrale de verdure millénaire."));
            
        return photos;
    }

    public static List<Journey> getTrendingJourneys() {
        List<Journey> journeys = new ArrayList<>();
        journeys.add(new Journey("1", "Le Sentier Alpin", "Zurich à Genève", "7 jours", "https://lh3.googleusercontent.com/aida-public/AB6AXuDgddeGrpbTEfi3PvJ_vkozCpAgw3EXoPaBGDsTRUy9ay3Dvhlf2rNIiHHwkO4JOXHQGo90VSDVBfavs6vGH7RGXtlwW2geKDE6eaT5GLC1hGJYCT-4OHGj0XBYBSWOrXyU709-w_Ovy1E1PfnKnAu_hlMdgBTxCigcChMvHiqY2Kbqr5EmpfTxkxGmRK7O-MzpaHwegfLaAhfjZUbAWARzCwihPG5CFBjLvSfOEsAr0QLVAuOOmP-jxGT3Z8JO1_1uZV5H9f2cL-S-", "EUROPE"));
        journeys.add(new Journey("2", "Merveilles Volcaniques", "Route Circulaire Sud", "5 jours", "https://lh3.googleusercontent.com/aida-public/AB6AXuB-j7qvqkwbL6Ar9lSb34lGBOCZocmUTNEz8kh3SNEBbPPXpxrdD_aaiQuIKC-vff9RYSTdtIPZrBihLx-NZ7T_SS0UxChAMnacFpMQ0WbnGoYR9b6CE414H1g3lZ5YowNYMXr3stWicl1TWmExQHiImLEe5AVBLnD04wEHyw4_GI_k67gayDjXrdTvSqfTJmuIBeIKYe-uYOUdaEQ1ZqjeXUa6LuY43mCYHpCsH3kSdsyAq07kO7RqDZ2arQXLPLoB_UwAZnSgpcz4", "ISLANDE"));
        journeys.add(new Journey("3", "Jardins Zen de Kyoto", "Circuit Culturel", "3 jours", "https://lh3.googleusercontent.com/aida-public/AB6AXuDXaxoJc-40wBeEf4DOO8V9g5KqFMJdFOH3PduSVKXhIEcLZicca_V1CrhcLn9uQJHavwkfimJpD1-WRSmccs9ZxSPd0wBfVwig0x8VYPizGASbUyeNHRAeu8cvSdE2GQ829lCv06fVaAhYYk7ozT-DRPpsgMEd_FJlR1-123Acg-7MKwSAzTYdPagf7EIA9FUkfnd3Ov9SYzbKjb6yj-Qj1p1MtpoNSwK_GH-URnWwEx0uJc7UFjM5fbprvJ0t54nM48qrmO7Xzb0G", "ASIE"));
        return journeys;
    }

    public static UserStats getUserStats() {
        return new UserStats("Niveau 4", 240, "Alexandre");
    }
}
