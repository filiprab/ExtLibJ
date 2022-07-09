package com.samourai.wallet.cahoots.multi;

import com.samourai.soroban.cahoots.CahootsContext;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsTypeUser;

public class MultiCahootsContext extends CahootsContext {
    private CahootsContext stonewallx2Context;
    private CahootsContext stowawayContext;

    public MultiCahootsContext(CahootsTypeUser typeUser, int account, Long amount, String address) {
        super(typeUser, CahootsType.MULTI, account, amount, address);
        this.stonewallx2Context = computeStonewallContext();
        this.stowawayContext = computeStowawayContext();
    }

    private CahootsContext computeStonewallContext() {
        if (getTypeUser().equals(CahootsTypeUser.COUNTERPARTY)) {
            return CahootsContext.newCounterpartyStonewallx2(getAccount());
        }
        return CahootsContext.newInitiatorStonewallx2(getAccount(), getAmount(), getAddress());
    }

    private CahootsContext computeStowawayContext() {
        if (getTypeUser().equals(CahootsTypeUser.COUNTERPARTY)) {
            return CahootsContext.newCounterpartyStowaway(getAccount());
        }
        long stowawayFee = computeMultiCahootsFee(getAmount());
        return CahootsContext.newInitiatorStowaway(getAccount(), stowawayFee);
    }

    private long computeMultiCahootsFee(long amount) {
        long stowawayFee = (long)(amount * 0.035d) + 400;
        if(stowawayFee > 1000000) {
            stowawayFee = 1000000;
        }
        return stowawayFee;
    }

    public CahootsContext getStonewallx2Context() {
        return stonewallx2Context;
    }

    public CahootsContext getStowawayContext() {
        return stowawayContext;
    }
}