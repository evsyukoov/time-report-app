const req = () => {
    let dateStart = document.getElementById("dateStart").value
    let dateEnd = document.getElementById("dateEnd").value
    let empl
    let dep
    let project
    let ulEmpl = document.getElementById("ul-id-dropdown-block-empl")
    let empChBox = document.getElementById("empl-report")
    let depChBox = document.getElementById("department-report")
    let projectChBox = document.getElementById("project-report")
    if (ulEmpl != null) {
        empl = ulEmpl.children[0].firstChild.nodeValue
    }
    let ulDep = document.getElementById("ul-id-dropdown-block-dep")
    if (ulDep != null) {
        dep = ulDep.children[0].firstChild.nodeValue
    }
    let ulProject = document.getElementById("ul-id-dropdown-block-project")
    if (ulProject != null) {
        project = ulProject.children[0].firstChild.nodeValue
    }
    //alert(dep + " " + empl + " " + dateStart + " " + dateEnd)

    // alert(depChBox.checked)

    var xhr = new XMLHttpRequest();
    xhr.open("POST", referer + "/time-report-app/report/get-report");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');

    var data = JSON.stringify({
        "name":
        empl,
        "project": project,
        "department": dep,
        "dateStart": parseDate(dateStart),
        "dateEnd": parseDate(dateEnd),
        "waitForEmployeeReport": empChBox.checked,
        "waitForDepartmentsReport": depChBox.checked,
        "waitForProjectReport": projectChBox.checked
    });
    xhr.responseType = 'blob'
    xhr.onloadstart = function (e) {
        document.getElementsByClassName("loader").item(0).style.display = "block";
    }
    xhr.onloadend = function (e) {
        document.getElementsByClassName("loader").item(0).style.display = "none";
    }
    xhr.onreadystatechange = function(oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status !== 200) {
                swal("Произошла ошибка на сервере. Сообщите поддержке данный ID " + xhr.getResponseHeader('Error-uuid'), {
                    icon: 'error'
                })
                return
            }
            let blob = new Blob([xhr.response], {type: 'application/vnd.ms-excel'});
            if (blob.size === 0) {
                swal('Отчетных дней не найдено. Смените фильтры поиска!', {
                    icon: 'error'
                })
                return
            }
            let link = document.createElement('a');
            link.download = 'Report-' + new Date().toISOString() + '.xls';
            link.href = URL.createObjectURL(blob);
            link.click();
            URL.revokeObjectURL(link.href);
        }
    };
    xhr.send(data)
}

function parseDate(dateStr) {
    let arr = dateStr.split(".")
    let day = arr[0];
    let month = arr[1]
    let year = arr[2]
    return Date.UTC(year, month - 1, day);
}