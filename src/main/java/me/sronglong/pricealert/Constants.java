package me.sronglong.pricealert;

public class Constants {

    // New Setting :

    //TOPIC
    public static final String CRYPTO_PRICE = "trading-events";
    public static final String CRYPTO_PRICE_aggregated = "trading-events-aggregated"; //trading information topic after being aggregated
    public static final String USER_PRICE_SETTING_TOPIC = "user-price-settings";
    public static final String USER_PRICE_SETTING_TOPIC_OUTPUT = "user-price-settings-output";
    public static final String USER_ALERTS = "user-alerts";
    public static final String USER_PRICE_LIST_OUTPUT = "user-price-list-output";
    public static final String PRODUCER_LIST = "producer-list";
    public static final String ALL_EXCHANGE_PAIR = "exchange-pairs";
    public static final String PRODUCER_NEW_TASK = "producer-new-tasks";
    public static final String PRODUCER_TASK_UPDATED = "producer-task-updated";


    //STATE_STORE
    public static final String PRICE_STATE_STORE = "price-state-store";
    public static final String USER_PRICE_STATE_STORE = "user-price-state-store";
    public static final String EXCHANGE_PAIR_STATE_STORES = "exchange_pair_state_store";
    public static final String PRODUCER_LIST_STATE_STORES = "producer_list_state_store";
    public static final String PRODUCER_PAIR_STATE_STORES = "producer_pair_state_store";





}
