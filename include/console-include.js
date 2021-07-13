let c = document.getElementById("console-area");
let urlSearchParams = new URLSearchParams(window.location.search);

let refreshConsole = async () => {
	let r = await Webinterface.call("server-manager", "getConsoleLog", {server: urlSearchParams.get("server")});
	if(!r.isSuccess()) return;
	let log = r.getData().log;
	if(log == null) {
		c.innerText = "Server is not running";
		return;
	}
	
	let scroll = c.scrollHeight - c.scrollTop == c.clientHeight;
	c.innerText = log.join("\n");
	if(scroll) c.scrollTop = c.scrollHeight;
};

setInterval(refreshConsole, 1000);
