

function runTest() {
    function now() {
        return "<span class=\"badge badge-secondary\">" + new Date().toISOString() + "_UTC</span>";
    }

    let result = document.querySelector('#result');

    let xhr = new XMLHttpRequest();
    let url = "/timelapse/test";
    xhr.open("GET", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);
            if (data.error) {
                let msg = "<span class=\"badge badge-danger\">" + data.error.msg + "</span>"
                let suggestion = "<p>" + data.error.suggestion + "</p>"
                result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + now() + msg + suggestion + "</li>";
            } else {
                result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + now() + "<span class=\"badge badge-primary\">" + data.result + "<span></li>";
            }
        }
    };
    result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + now() + "<span class=\"badge badge-success\">Commence test run<span></li>";
    xhr.send();
}