package com.baidu.ibase.xslan;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class XslanPublisher extends Notifier {
	@DataBoundConstructor
	public XslanPublisher(String xslFile, String xmlFile, String outFile,
			boolean isOutToEmailExt) {
		this.xslFile = xslFile;
		this.xmlFile = xmlFile;
		this.outFile = outFile;
		// 增加参数存储解决该属性不变问题
		this.isOutToEmailExt = isOutToEmailExt;
	}

	private static final Logger LOGGER = Logger.getLogger(XslanPublisher.class
			.getName());

	// private final boolean byMail
	private String xslFile;
	private String xmlFile;
	private String outFile;
	private boolean isOutToEmailExt;

	public String getXslFile() {
		return xslFile;
	}

	public String getXmlFile() {
		return xmlFile;
	}

	public String getOutFile() {
		return outFile;
	}

	public boolean getIsOutToEmailExt() {
		return isOutToEmailExt;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		LOGGER.log(Level.INFO, "perform xsl execute");
		if (xslFile == null || xmlFile == null || outFile == null) {
			LOGGER.log(Level.INFO, "not set for xsl, xml or out file");
			return true;
		}

		try {
			// 文件不存在时会抛空指针异常，增加校验逻辑
			if (!build.getWorkspace().child(xslFile).exists()
					|| !build.getWorkspace().child(xmlFile).exists()) {
				LOGGER.info("xsl or xml not exist");
				return true;
			}

			StreamSource xsl = new StreamSource(build.getWorkspace()
					.child(xslFile).read());
			StreamSource xml = new StreamSource(build.getWorkspace()
					.child(xmlFile).read());
			StreamResult out = new StreamResult(build.getWorkspace()
					.child(outFile).write());

			TransformerFactory.newInstance().newTransformer(xsl)
					.transform(xml, out);
		} catch (TransformerConfigurationException e) {
			LOGGER.log(Level.WARNING, "exception", e);
		} catch (TransformerFactoryConfigurationError e) {
			LOGGER.log(Level.WARNING, "exception", e);
		} catch (TransformerException e) {
			LOGGER.log(Level.WARNING, "exception", e);
		}
		return true;
	}

	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final XslanPublisherDescriptor DESCRIPTOR = new XslanPublisherDescriptor();

	public static final class XslanPublisherDescriptor extends
			BuildStepDescriptor<Publisher> {

		private String xslFile = null;
		private String xmlFile = null;
		private String outFile = null;
		private boolean isOutToEmailExt = false;

		@Override
		public String getDisplayName() {
			return "Xsl Extension with xalan";
		}

		public String getXslFile() {
			return xslFile;
		}

		public String getXmlFile() {
			return xmlFile;
		}

		public String getOutFile() {
			return outFile;
		}

		public boolean getIsOutToEmailExt() {
			return isOutToEmailExt;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			LOGGER.log(Level.INFO, formData.toString());

			req.bindParameters(this, "xslan.");
			save();
			return super.configure(req, formData);
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			LOGGER.info(formData.toString());
			XslanPublisher publisher = req.bindJSON(XslanPublisher.class,
					formData);
			return publisher;
		}

		public boolean isEmailExtInstalled() {
			return Hudson.getInstance().pluginManager.getPlugin("email-ext") != null;
		}
	}
}
