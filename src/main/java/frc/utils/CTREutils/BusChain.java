package frc.utils.CTREutils;

public enum BusChain {

    CANBUS("rio"),
    CANIVORE("CANivore");

    private final String chainName;

    BusChain(String chainName){
        this.chainName = chainName;
    }

    public String getChainName() {
        return chainName;
    }
}