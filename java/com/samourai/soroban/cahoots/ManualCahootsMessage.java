package com.samourai.soroban.cahoots;

import com.samourai.wallet.cahoots.Cahoots;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsTypeUser;
import com.samourai.soroban.client.SorobanMessage;

public class ManualCahootsMessage implements SorobanMessage {
    private Cahoots cahoots;

    public static final int LAST_STEP = 4;
    public static final int LAST_STEP_MULTI = 8;
    public static final int NB_STEPS = LAST_STEP+1; // starting from 0
    public static final int NB_STEPS_MULTI = LAST_STEP_MULTI+1; // starting from 0

    public ManualCahootsMessage(Cahoots cahoots) {
        this.cahoots = cahoots;
    }

    public static ManualCahootsMessage parse(String payload) throws Exception {
        return new ManualCahootsMessage(Cahoots.parse(payload));
    }

    public int getStep() {
        return cahoots.getStep();
    }

    public int getNbSteps() {
        return getType() == CahootsType.MULTI ? NB_STEPS_MULTI : NB_STEPS;
    }

    @Override
    public boolean isDone() {
        return this.getType() == CahootsType.MULTI ? getStep() == LAST_STEP_MULTI : getStep() == LAST_STEP;
    }

    public CahootsType getType() {
        return CahootsType.find(cahoots.getType()).get();
    }

    public CahootsTypeUser getTypeUser() {
        if (getStep()%2 == 0) {
            return CahootsTypeUser.SENDER;
        }
        return CahootsTypeUser.COUNTERPARTY;
    }

    @Override
    public String toPayload() {
        return cahoots.toJSONString();
    }

    public Cahoots getCahoots() {
        return cahoots;
    }

    @Override
    public String toString() {
        return "(ManualCahootsMessage)step="+getStep()+"/"+getNbSteps()+", type="+getType()+", typeUser="+getTypeUser()+", payload="+toPayload();
    }
}
