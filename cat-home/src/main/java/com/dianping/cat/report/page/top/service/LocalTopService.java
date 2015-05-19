package com.dianping.cat.report.page.top.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopReportMerger;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalTopService extends LocalModelService<TopReport> {

	public static final String ID = TopAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		List<TopReport> reports = super.getReport(period, domain);
		TopReport report = null;

		if (reports != null) {
			report = new TopReport(domain);
			TopReportMerger merger = new TopReportMerger(report);

			for (TopReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getDomains().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new TopReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		return new TopReportFilter().buildXml(report);
	}

	private TopReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, TopAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class TopReportFilter extends com.dianping.cat.consumer.top.model.transform.DefaultXmlBuilder {
		public TopReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
