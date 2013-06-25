package com.otheri.tbwatcher;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.otheri.commons.Consts;
import com.otheri.oop.server.anotation.ServiceMethod;
import com.otheri.oop.server.anotation.ServiceName;
import com.otheri.tbwatcher.entity.Compare;
import com.otheri.tbwatcher.entity.Compare.NamePair;
import com.otheri.tbwatcher.entity.Compare.SkuPair;
import com.otheri.tbwatcher.entity.Sample;
import com.otheri.tbwatcher.entity.Seed;
import com.otheri.tbwatcher.entity.Sku;

//type名映射重复，这个类可能不会被注册
@ServiceName(name = "tbwatcher")
public class TBWatcherService {

	@ServiceMethod(name = "startServer", type = Consts.METHOD_POST)
	public void startServer() throws Exception {
		System.out.println("xxxxxxxxxxxxxx --startServer");
	}

	@ServiceMethod(name = "stopServer", type = Consts.METHOD_POST)
	public void stopServer() throws Exception {
		System.out.println("xxxxxxxxxxxxxx --stopServer");
	}

	@ServiceMethod(name = "getSeeds", type = Consts.METHOD_POST)
	public ArrayList<Seed> getSeeds() throws Exception {
		System.out.println("xxxxxxxxxxxxxx --getSeeds");
		return Utils.listSeeds();
	}

	@ServiceMethod(name = "newSeed", type = Consts.METHOD_POST)
	public void newSeed(String url, String desc) throws Exception {
		System.out.println("xxxxxxxxxxxxxx --newSeed");

		Document doc = Jsoup.connect(url).get();
		Seed seed = Utils.getSeed(doc, url, desc);
		Utils.writeSeed(seed);
	}

	@ServiceMethod(name = "removeSeed", type = Consts.METHOD_POST)
	public void removeSeed(String url) throws Exception {
		System.out.println("xxxxxxxxxxxxxx --removeSeed");
		Utils.removeSeedAndSample(url);
	}

	@ServiceMethod(name = "compare", type = Consts.METHOD_POST)
	public Compare compare(String url) throws Exception {
		System.out.println("xxxxxxxxxxxxxx --compare");

		Compare compare = new Compare();
		compare.lastSample = Utils.readSample(url);

		Document doc = Jsoup.connect(url).get();
		compare.currSample = Utils.getSample(doc, url);

		if (compare.currSample == null) {
			throw new Exception("抓取数据失败，无法继续比较!");
		} else {
			if (compare.lastSample == null) {
				// 没有老数据可以比较，刷新
				Utils.writeSample(compare.currSample);
				compare.namePair = new NamePair("", compare.currSample.name);

				for (Sku sku : compare.currSample.skus) {
					SkuPair pair = new SkuPair(SkuPair.NEW, sku.skuId,
							sku.price, sku.price, sku.stock, sku.stock);
					compare.skuPairs.add(pair);

				}
			} else {
				// 开始对比
				compare.namePair = new NamePair(compare.lastSample.name,
						compare.currSample.name);

				for (Sku lastSku : compare.lastSample.skus) {
					// 遍历旧的样本
					Sku temp = null;
					for (Sku currSku : compare.currSample.skus) {
						if (lastSku.skuId.equals(currSku.skuId)) {
							temp = currSku;
							break;
						} else {
							continue;
						}
					}

					if (null != temp) {
						// 找到
						SkuPair pair = new SkuPair(SkuPair.EQUALITY,
								lastSku.skuId, lastSku.price, temp.price,
								lastSku.stock, temp.stock);
						compare.skuPairs.add(pair);
					} else {
						// 没找到，被删除
						SkuPair pair = new SkuPair(SkuPair.DELETED,
								lastSku.skuId, lastSku.price, "",
								lastSku.stock, "");
						compare.skuPairs.add(pair);

					}
				}

				for (Sku currSku : compare.currSample.skus) {
					boolean isNew = true;
					for (Sku lastSku : compare.lastSample.skus) {
						if (currSku.skuId.equals(lastSku.skuId)) {
							isNew = false;
							break;
						} else {
							continue;
						}
					}
					if (isNew) {
						// 找到相同的，新增
						SkuPair pair = new SkuPair(SkuPair.NEW, currSku.skuId,
								"", currSku.price, "", currSku.stock);
						compare.skuPairs.add(pair);
					}
				}
			}
		}

		return compare;
	}

	@ServiceMethod(name = "saveCurrentSample", type = Consts.METHOD_POST)
	public void saveCurrentSample(Sample sample) throws Exception {
		System.out.println("xxxxxxxxxxxxxx --saveCurrentSample");

		Utils.writeSample(sample);
	}

	// private static final String mongo =
	// "mongodb://zhangyu:is333ocean@ds027318.mongolab.com:27318/tbwatcher";
	//
	// @ServiceMethod(name = "newSeed", type = Consts.METHOD_POST)
	// public void newSeed(String url, String desc) throws Exception {
	// System.out.println("xxxxxxxxxxxxxx --newSeed");
	//
	// MongoClient mongoClient = new MongoClient(new MongoClientURI(mongo));
	// try {
	// DB db = mongoClient.getDB("tbwatcher");
	// DBCollection coll = db.getCollection("tbseed");
	//
	// DBObject criteria = new BasicDBObject("url", url);
	//
	// DBObject seedobj = new BasicDBObject();
	//
	// Document doc = Jsoup.connect(url).get();
	//
	// Seed seed = Utils.getSeed(doc, url, desc);
	//
	// seedobj.put("name", seed.name);
	// seedobj.put("url", seed.url);
	// seedobj.put("desc", seed.desc);
	// seedobj.put("imgUrl", seed.imgUrl);
	//
	// BasicDBList skuList = new BasicDBList();
	// for (Sku sku : seed.skus) {
	// DBObject skuobj = new BasicDBObject();
	// skuobj.put("skuId", sku.skuId);
	// skuobj.put("price", sku.price);
	// skuobj.put("stock", sku.stock);
	// skuList.add(skuobj);
	// }
	// seedobj.put("skus", skuList);
	//
	// DBObject objNew = new BasicDBObject("$set", seedobj);
	//
	// WriteResult wr = coll.update(criteria, objNew, true, false);
	// if (wr.getN() > 0) {
	// // 成功
	// } else {
	// throw new Exception(wr.getError());
	// }
	// } finally {
	// mongoClient.close();
	// }
	//
	// }
	//
	// @ServiceMethod(name = "getSeeds", type = Consts.METHOD_POST)
	// public ArrayList<Seed> getSeeds() throws Exception {
	// System.out.println("xxxxxxxxxxxxxx --getSeeds");
	//
	// MongoClient mongoClient = new MongoClient(new MongoClientURI(mongo));
	// DB db = mongoClient.getDB("tbwatcher");
	// DBCollection coll = db.getCollection("tbseed");
	//
	// DBCursor cursor = coll.find();
	// ArrayList<Seed> seeds = new ArrayList<Seed>();
	// try {
	// while (cursor.hasNext()) {
	// DBObject dbo = cursor.next();
	// Seed seed = new Seed();
	//
	// seed.name = dbo.get("name").toString();
	// seed.url = dbo.get("url").toString();
	// seed.imgUrl = dbo.get("imgUrl").toString();
	// seed.desc = dbo.get("desc").toString();
	//
	// BasicDBList skuList = (BasicDBList) dbo.get("skus");
	// for (int i = 0; i < skuList.size(); i++) {
	// BasicDBObject obj = (BasicDBObject) skuList.get(i);
	// Sku sku = new Sku();
	// sku.skuId = obj.getString("skuId");
	// sku.price = obj.getString("price");
	// sku.stock = obj.getString("stock");
	// seed.skus.add(sku);
	// }
	//
	// BSONObject b;
	//
	// seeds.add(seed);
	// }
	// } finally {
	// cursor.close();
	// }
	//
	// mongoClient.close();
	//
	// return seeds;
	// }
	//
	// @ServiceMethod(name = "removeSeed", type = Consts.METHOD_POST)
	// public void removeSeed(String url) throws Exception {
	// System.out.println("xxxxxxxxxxxxxx --removeSeed");
	//
	// MongoClient mongoClient = new MongoClient(new MongoClientURI(mongo));
	// try {
	// DB db = mongoClient.getDB("tbwatcher");
	// DBCollection coll = db.getCollection("tbseed");
	//
	// DBObject criteria = new BasicDBObject("url", url);
	//
	// WriteResult wr = coll.remove(criteria);
	// if (wr.getN() > 0) {
	// // 成功
	// } else {
	// throw new Exception(wr.getError());
	// }
	// } finally {
	// mongoClient.close();
	// }
	//
	// }
}
