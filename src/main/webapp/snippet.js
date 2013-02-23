Event.observe(window, "load", function() {
	prettyPrint();
	$("deleteButton").observe("click", function(evt) {
		$('delete-all').toggleClassName("hidden");
	});
});