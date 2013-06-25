package com.obfuskate.accountbalance;

import java.util.Date;

public class RecentTransaction {
  public Date transactionDate;
  public String transactionPlace;
  public long transactionAmount;
  public long rowId;
  
  public RecentTransaction(long time, String place, long amount, long rowId) {
    this.transactionPlace = place;
    this.transactionAmount = amount;
    this.transactionDate = new Date(time * 1000);
    this.rowId = rowId;
  }
}
