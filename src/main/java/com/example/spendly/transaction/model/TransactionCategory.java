package com.example.spendly.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCategory {

    // Food and groceries
    GROCERIES("Groceries"),
    CONVENIENCE_STORE("Convenience store"),
    RESTAURANTS_AND_CAFES("Restaurants and cafes"),
    FAST_FOOD("Fast food"),
    STREET_FOOD("Street food"),
    COFFEE_AND_SNACKS("Coffee and snacks"),
    FOOD_DELIVERY("Food delivery"),
    VENDING_MACHINE("Vending machine"),

    // Markets, fairs and travel retail
    MARKETS_AND_FAIRS("Markets and fairs"),
    SECOND_HAND_MARKET("Second-hand market"),
    DUTY_FREE("Duty free"),
    TRAVEL_RETAIL("Travel retail"),

    // Transport and travel
    PUBLIC_TRANSPORT("Public transport"),
    TAXI_AND_RIDESHARING("Taxi and ridesharing"),
    FLIGHTS("Flights"),
    TRAVEL_BOOKING("Travel booking"),
    HOTELS_AND_ACCOMMODATION("Hotels and accommodation"),
    PARKING("Parking"),

    // Housing and household
    RENT("Rent"),
    UTILITIES("Utilities"),
    HOME_AND_HOUSEHOLD("Home and household"),
    HOUSEHOLD_SUPPLIES("Household supplies"),
    HOME_IMPROVEMENT("Home improvement"),
    LAUNDRY("Laundry"),

    // Mobile, internet and subscriptions
    MOBILE_AND_INTERNET("Mobile and internet"),
    SUBSCRIPTIONS("Subscriptions"),
    STREAMING_SERVICES("Streaming services"),
    SOFTWARE("Software"),
    DIGITAL_TOOLS("Digital tools"),
    AI_TOOLS("AI tools"),

    // Shopping
    GENERAL_SHOPPING("General shopping"),
    CLOTHING("Clothing"),
    SHOES_AND_ACCESSORIES("Shoes and accessories"),
    ELECTRONICS("Electronics"),
    SPORT_AND_OUTDOOR("Sport and outdoor"),
    BOOKS_AND_MEDIA("Books and media"),

    // Health and personal care
    PHARMACY("Pharmacy"),
    HEALTHCARE("Healthcare"),
    BEAUTY_AND_PERSONAL_CARE("Beauty and personal care"),
    BARBERSHOP("Barbershop"),
    SPORT_AND_FITNESS("Sport and fitness"),
    SPORT_NUTRITION("Sport nutrition"),

    // Entertainment and lifestyle
    ENTERTAINMENT("Entertainment"),
    AMUSEMENT_PARKS("Amusement parks"),
    CONCERTS_AND_EVENTS("Concerts and events"),
    TICKETS_AND_EVENTS("Tickets and events"),
    CINEMA_AND_THEATRE("Cinema and theatre"),
    NIGHTLIFE("Nightlife"),
    GAMING("Gaming"),
    DIGITAL_MARKETPLACE("Digital marketplace"),

    // Education
    EDUCATION("Education"),
    LANGUAGE_LEARNING("Language learning"),

    // Government and finance
    GOVERNMENT_AND_MUNICIPALITY("Government and municipality"),
    TAXES_AND_GOVERNMENT("Taxes and government"),
    BANK_FEES("Bank fees"),
    CASH_WITHDRAWAL("Cash withdrawal"),
    CURRENCY_EXCHANGE("Currency exchange"),

    // Investments and financial products
    INVESTMENTS("Investments"),
    CRYPTO("Crypto"),
    SAVINGS("Savings"),
    GAMBLING("Gambling"),

    // Transfers
    INTERNAL_TRANSFER("Internal transfer"),
    EXTERNAL_TRANSFER("External transfer"),
    PERSONAL_TRANSFER("Personal transfer"),
    FRIEND_TRANSFER("Friend transfer"),
    FAMILY_TRANSFER("Family transfer"),

    // Income and returns
    SALARY("Salary"),
    SCHOLARSHIP("Scholarship"),
    GOVERNMENT_SUPPORT("Government support"),
    FREELANCE_INCOME("Freelance income"),
    GIFT_RECEIVED("Gift received"),
    BANK_GIFT("Bank gift"),
    CASHBACK("Cashback"),
    REFUND("Refund"),
    OTHER_INCOME("Other income"),

    // Special / unclear cases
    PAYMENT_ON_BEHALF("Payment on behalf"),
    REIMBURSEMENT("Reimbursement"),
    NEEDS_REVIEW("Needs review"),
    OTHER("Other"),
    UNCATEGORIZED("Uncategorized");

    private final String displayName;
}