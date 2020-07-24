

function setupIndexPage() {
    getKeyFrameIds()
    setDatetimePicker()
}

function toLocalDateTimeString(d) {
    let datestring = d.getFullYear() + "-" + ("0"+(d.getMonth()+1)).slice(-2) + "-" + ("0" + d.getDate()).slice(-2) + "T" + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2) + ":" + ("0" + d.getSeconds()).slice(-2);
    console.log("LocalDT: " + datestring)
    return datestring
}

function nowHTML() {
    return "<span class=\"badge badge-secondary\">" + toLocalDateTimeString(new Date()) + " UTC</span>";
}

function timeHTML(time) {
    return "<span class=\"badge badge-secondary\">" + toLocalDateTimeString(time) + "</span>"
}

function setDatetimePicker() {
    let dt = document.querySelector('#startAt');
    let nowDt = toLocalDateTimeString(new Date())
    console.log("Datepicker: " + nowDt)
    dt.value = nowDt
}

function getKeyFrameIds() {
    let dropdown = document.querySelector('#key-frame-ids');

    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/key-frames", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);
            data.forEach(function(t){
                let o = document.createElement('option');
                o.value = t
                o.innerHTML = t
                dropdown.appendChild(o);
            })
        }
    };
    xhr.send();
}

function getSelectedKeyFrameId() {
    let kf = document.querySelector('#key-frame-ids');
    return kf.options[kf.selectedIndex].value;
}

function getDatetimePickerValue() {
    let dt = document.querySelector('#startAt')
    let localDt = new Date(dt.value)
    console.log("startAt UTC: " + localDt.toISOString())
    return localDt.toISOString()
}

function storeTimelapseTask(url) {
    let result = document.querySelector('#storedTasks');

    function newLog(t) {
        let settings = "s: " + t.shutterSpeed + ", i: " + t.iso + ", a: " + t.aperture + ", ev: " + t.ev
        let testClass = ((t.test) ? "test-task" : "")
        return "<li class=\"list-group-item " + testClass + "\">" + timeHTML(new Date(t.timestamp)) + "<span class=\"badge badge-primary\">" + settings + "<span></li>";
    }

    let xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);
            data.forEach(function(t){
                result.innerHTML = newLog(t) + result.innerHTML;
            })
        }
    };
    result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + nowHTML() + "<span class=\"badge badge-success\">Commence test run<span></li>";
    xhr.send();
}

function storeTimelapse() {
    let startAt = document.querySelector('#startAt').value;
    let dtStartAt = new Date(startAt).toISOString()
    console.log("StartAt: " + dtStartAt)
    storeTimelapseTask("/timelapse/" + getSelectedKeyFrameId() + "/" + dtStartAt)
}

function runTest() {
    storeTimelapseTask("/timelapse/test/" + getSelectedKeyFrameId())
}

const rateFetchCaptured = 1000;

function getTimeForNextFetchCaptured() {
    let now = new Date();
    return new Date(now - rateFetchCaptured).toISOString()
}

function getCapturedTasks() {
    let result = document.querySelector('#executedTasks');

    function newLog(c) {
        let testClass = ((c.test) ? "test-task" : "");
        if (c.error) {
            let msg = "<span class=\"badge badge-danger\">" + c.error + "</span>"
            let suggestion = "<p>" + c.suggestion + "</p>"
            return "<li class=\"list-group-item " + testClass + "\">" + timeHTML(new Date(c.timestamp)) + msg + suggestion + "</li>";
        } else {
            let settings = "s: " + c.shutterSpeed + ", i: " + c.iso + ", a: " + c.aperture + ", ev: " + c.ev
            return "<li class=\"list-group-item " + testClass + "\">" + timeHTML(new Date(c.timestamp)) + "<span class=\"badge badge-primary\">" + settings + "<span></li>";
        }
    }

    let xhr = new XMLHttpRequest();
    let url = "/timelapse/captured/" + getTimeForNextFetchCaptured();
    xhr.open("GET", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);

            if (data.length > 0) {
                data.forEach(function(c) {
                    result.innerHTML = newLog(c) + result.innerHTML;
                })
            }
        }
    };
    xhr.send();
}

setInterval(getCapturedTasks, rateFetchCaptured);