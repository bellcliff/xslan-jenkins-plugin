/**
 * <li>if emailext installed and emailext config checked, output will checked
 * auto
 */
(function() {
	//
	var loaded = false,
	// run after load
	f, onload = function() {
		if (loaded)
			return;
		loaded = true;
		f = document.getElementsByName("config")[0];
		supportHtmlPublisher();
		supportEmailExtPlugin();
	};

	//
	function supportHtmlPublisher() {

	}
	//
	function supportEmailExtPlugin() {

		var checkbox = f["hudson-plugins-emailext-ExtendedEmailPublisher"]
		var changeHandle = function() {
			var textarea = f["project_default_content"];
			console.log(textarea.value);
			var change = function(){
				// 判断是否指向文件，如果是，修改xslan中的配置项
				if (/FILE, path=\"([^"]+)\"/.test(textarea.value)
						&& f["xslan.isOutToEmailExt"].checked) {
					if (f["xslan.isOutToEmailExt"].checked) {
						f["xslan.outFile"].value = RegExp.$1;
					}
				}
			};
			Event.observe(textarea, "change", change);
			change();
		}
		if (checkbox) {
			if (!checkbox.checked) {
				Event.observe(checkbox, "click", function() {// 元素后添加
					setTimeout(changeHandle, 500);
				});
			} else
				changeHandle();
		}
		function outToEmailExt() {
			if (f["xslan.isOutToEmailExt"].checked) {
				f["xslan.outFile"].readOnly = true;
				changeHandle();
			} else
				f["xslan.outFile"].readOnly = false;
		}
		Event.observe(f["xslan.isOutToEmailExt"], "click", outToEmailExt);
//		outToEmailExt();
	}

	Event.observe(document, "dom:loaded", onload);
	Event.observe(window, "load", onload);

})();
