package com.otheri.tbwatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.otheri.commons.io.Input;
import com.otheri.commons.io.Output;
import com.otheri.tbwatcher.entity.Sample;
import com.otheri.tbwatcher.entity.Seed;
import com.otheri.tbwatcher.entity.Sku;

public class Utils {

	public static void main(String[] args) {
		try {
			String test = "http://item.taobao.com/item.htm?spm=a230r.1.14.115.UXAIbR&id=24272392535";
			// String test = "http://detail.tmall.com/item.htm?id=18643393007";
			// String test =
			// "http://detail.tmall.com/item.htm?id=19276752117&spm=a230r.1.14.17.8CCLbU&ad_id=&am_id=&cm_id=140105335569ed55e27b&pm_id=";

			Document doc = Jsoup.connect(test).get();

			Seed seed = getSeed(doc, test, "");

			Sample sample = getSample(doc, test);

			writeSeed(seed);
			writeSample(sample);

			Seed newSeed = readSeed(new File(getSeedFileName(seed.url)));
			Sample newSample = readSample(sample.url);

			System.out.println();

			// removeSeedAndSample(seed.url);

			// tt();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// public static void tt() throws Exception {
	// MongoClientURI dbAddress = new MongoClientURI(
	// "mongodb://zhangyu:is333ocean@ds027318.mongolab.com:27318/tbwatcher");
	// MongoClient mongoClient = new MongoClient(dbAddress);
	// DB db = mongoClient.getDB("tbwatcher");
	//
	// DBCollection coll = db.getCollection("tbseed");
	// System.out.println(coll.getCount());
	//
	// // DBObject myDoc = coll.findOne();
	// // System.out.println(myDoc);
	// System.out.println("..................");
	//
	// mongoClient.close();
	// }

	private static final String SEED_ROOT = "seeds/";
	private static final String SAMPLE_ROOT = "samples/";

	public static String getSeedFileName(String url)
			throws UnsupportedEncodingException {
		File seedRoot = new File(SEED_ROOT);
		if (!seedRoot.exists()) {
			seedRoot.mkdirs();
		}
		return SEED_ROOT + URLEncoder.encode(url, "UTF-8");
	}

	public static void writeSeed(Seed seed) throws IOException {
		String fileName = getSeedFileName(seed.url);

		File file = new File(fileName);

		FileOutputStream fos = new FileOutputStream(file);
		Output out = new Output(fos);
		byte[] data = JSON.toJSONBytes(seed, SerializerFeature.SortField);
		out.write(data);
		out.flush();
		out.close();
		fos.close();
	}

	public static Seed readSeed(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		Input in = new Input(fis);
		byte[] data = in.readAll();
		in.close();
		Seed ret = (Seed) JSON.parseObject(data, Seed.class);
		return ret;
	}

	public static ArrayList<Seed> listSeeds() throws IOException {
		File seedRoot = new File(SEED_ROOT);
		ArrayList<Seed> ret = new ArrayList<Seed>();
		File[] files = seedRoot.listFiles();
		if (files != null) {
			for (File file : files) {
				Seed seed = readSeed(file);
				ret.add(seed);
			}
		}
		return ret;
	}

	public static String getSampleFileName(String url)
			throws UnsupportedEncodingException {
		File sampleRoot = new File(SAMPLE_ROOT);
		if (!sampleRoot.exists()) {
			sampleRoot.mkdirs();
		}
		return SAMPLE_ROOT + URLEncoder.encode(url, "UTF-8");
	}

	public static void writeSample(Sample sample) throws IOException {
		String fileName = getSampleFileName(sample.url);

		File file = new File(fileName);

		FileOutputStream fos = new FileOutputStream(file);
		Output out = new Output(fos);
		byte[] data = JSON.toJSONBytes(sample, SerializerFeature.SortField);
		out.write(data);
		out.flush();
		out.close();
		fos.close();
	}

	public static Sample readSample(String fileName) {
		try {
			FileInputStream fis = new FileInputStream(new File(
					getSampleFileName(fileName)));
			Input in = new Input(fis);
			byte[] data = in.readAll();
			in.close();
			fis.close();
			return (Sample) JSON.parseObject(data, Sample.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void removeSeedAndSample(String url) throws IOException {
		String seedFileName = getSeedFileName(url);
		String sampleFileName = getSampleFileName(url);

		File seedFile = new File(seedFileName);
		File sampleFile = new File(sampleFileName);

		seedFile.delete();
		sampleFile.delete();
	}

	public static String selectFirstElement2String(Document doc, String query)
			throws Exception {
		Elements es = doc.select(query);
		if (es.size() > 0) {
			return Jsoup.clean(doc.select(query).first().html(),
					Whitelist.basic());
		} else {
			throw new Exception("query empty @ " + query);
		}
	}

	public static String selectFirstElementAttr2String(Document doc,
			String query, String attr) throws Exception {
		Elements es = doc.select(query);
		if (es.size() > 0) {
			return Jsoup.clean(doc.select(query).first().attr(attr),
					Whitelist.basic());
		} else {
			throw new Exception("query empty @ " + query);
		}
	}

	public static Sample getSample(Document doc, String url) throws Exception {

		System.out.println("sample-----start");

		Sample sample = new Sample();
		sample.url = url;

		try {
			// 天猫
			sample.name = selectFirstElement2String(doc, "div.tb-detail-hd a");
		} catch (Exception e) {
			// 淘宝
			sample.name = selectFirstElement2String(doc, "div.tb-detail-hd h3");
		}

		System.out.println("name:" + sample.name);

		Elements es = doc.select("script");
		Iterator<Element> it = es.iterator();
		while (it.hasNext()) {
			Element e = it.next();

			String html = e.html();
			if (html.contains("valItemInfo") && html.contains("skuMap")) {
				// System.out.println(">>>" + html);
				int start = html.indexOf("skuMap");
				if (start >= 0) {
					for (;;) {
						int ind = html.indexOf("\"skuId\"", start)
								+ "\"skuId\"".length();
						int st;
						int et;
						if (ind > start) {

							Sku sku = new Sku();
							// skuId
							ind = html.indexOf(":", ind);
							st = html.indexOf("\"", ind) + 1;
							et = html.indexOf("\"", st);
							sku.skuId = html.substring(st, et);

							// System.out.println("skuId=" + sku.skuId);

							start = et;

							// price
							ind = html.indexOf("\"price\"", start);
							ind = html.indexOf(":", ind);
							st = html.indexOf("\"", ind) + 1;
							et = html.indexOf("\"", st);
							sku.price = html.substring(st, et);

							// System.out.println("   price=" + sku.price);

							start = et;

							// stock
							ind = html.indexOf("\"stock\"", start);
							ind = html.indexOf(":", ind);
							st = html.indexOf("\"", ind) + 1;
							et = html.indexOf("\"", st);
							sku.stock = html.substring(st, et);

							// System.out.println("   stock=" + sku.stock);

							start = et;

							sample.skus.add(sku);
						} else {
							break;
						}
					}

				}
			} else {
				// System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
			}

		}

		System.out.println("sample-----end");

		return sample;
	}

	/**
	 * 有些东西不好抓，因为是异步获取，例如30天售出数量
	 */
	public static Seed getSeed(Document doc, String url, String desc)
			throws Exception {

		Seed seed = new Seed();

		seed.url = url;
		seed.desc = desc;

		System.out.println("seed-----start");

		try {
			// 天猫
			seed.imgUrl = selectFirstElementAttr2String(doc,
					"div.tb-gallery span", "src");
		} catch (Exception e) {
			// 淘宝
			seed.imgUrl = selectFirstElementAttr2String(doc,
					"div.tb-gallery img", "src");
		}

		System.out.println("url:" + seed.url);
		System.out.println("img:" + seed.imgUrl);

		System.out.println("seed-----end");
		return seed;
	}

}
