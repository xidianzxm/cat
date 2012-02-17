package com.dianping.cat.job.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.HdfsDumpConsumer;
import com.dianping.cat.job.hdfs.OutputChannelManager;
import com.dianping.cat.job.hdfs.DefaultOutputChannelManager;
import com.dianping.cat.job.hdfs.DefaultOutputChannel;
import com.dianping.cat.job.hdfs.HdfsMessageStorage;
import com.dianping.cat.job.hdfs.OutputChannel;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		if (isEnv("dev") || property("env", null) == null) {
			all.add(C(OutputChannel.class, DefaultOutputChannel.class).is(PER_LOOKUP) //
			      .req(MessageCodec.class, "plain-text") //
			      .config(E("maxSize").value(String.valueOf(2 * 1024 * 1024L))));
			all.add(C(OutputChannelManager.class, DefaultOutputChannelManager.class) //
			      .req(MessagePathBuilder.class));
		} else {
			all.add(C(OutputChannel.class, DefaultOutputChannel.class).is(PER_LOOKUP) //
			      .req(MessageCodec.class, "plain-text") //
			      .config(E("maxSize").value(String.valueOf(128 * 1024 * 1024L))));
			all.add(C(OutputChannelManager.class, DefaultOutputChannelManager.class) //
			      .req(MessagePathBuilder.class) //
			      .config(E("baseDir").value("data"), //
			            E("serverUri").value("/catlog")));
		}

		all.add(C(MessageStorage.class, "hdfs", HdfsMessageStorage.class) //
		      .req(OutputChannelManager.class));
		all.add(C(MessageConsumer.class, HdfsDumpConsumer.ID, HdfsDumpConsumer.class) //
		      .req(MessageStorage.class, "hdfs"));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
