package com.otheri.tbwatcher.entity;

import java.util.ArrayList;

public class Compare {

	public Sample lastSample;
	public Sample currSample;

	public NamePair namePair;
	public ArrayList<SkuPair> skuPairs = new ArrayList<SkuPair>();

	private static boolean changed(String v1, String v2) {
		return !v1.equals(v2);
	}

	public static class NamePair {

		public boolean changed;
		public String oldValue;
		public String newValue;

		public NamePair(String oldValue, String newValue) {
			this.oldValue = oldValue;
			this.newValue = newValue;

			changed = changed(oldValue, newValue);
		}

	}

	public static class SkuPair {

		public static final int EQUALITY = 0;
		public static final int DIFFERENT = 1;
		public static final int DELETED = 2;
		public static final int NEW = 3;

		public int skuFlg;
		public String skuId;

		public boolean priceChanged;
		public String oldPrice;
		public String newPrice;

		public boolean stockChanged;
		public String oldStock;
		public String newStock;

		public SkuPair(int skuFlg, String skuId, String oldPrice,
				String newPrice, String oldStock, String newStock) {
			this.skuFlg = skuFlg;
			this.skuId = skuId;

			this.oldPrice = oldPrice;
			this.newPrice = newPrice;
			priceChanged = changed(oldPrice, newPrice);

			this.oldStock = oldStock;
			this.newStock = newStock;
			stockChanged = changed(oldStock, newStock);
		}

	}

}
