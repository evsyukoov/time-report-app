
const req = () => {
    let dateStart = document.getElementById("dateStart").value
    let dateEnd = document.getElementById("dateEnd").value
    let empl
    let dep
    let ulEmpl = document.getElementById("ul-iddropdown-block-empl")
    if (ulEmpl != null) {
        empl = ulEmpl.children[0].firstChild.nodeValue
    }
    let ulDep = document.getElementById("ul-iddropdown-block-dep")
    if (ulDep != null) {
        dep = ulDep.children[0].firstChild.nodeValue
    }
    //alert(dep + " " + empl + " " + dateStart + " " + dateEnd)
    var formData = new FormData()
    formData.append('name', empl)
    formData.append('department', dep)

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "http://localhost:8082/report/get-report");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    var data = JSON.stringify({ "name":
        empl, "department": dep, "dateStart": Date.parse(dateStart), "dateEnd": Date.parse(dateEnd) });
    xhr.responseType = 'blob'
    xhr.onload = function(oEvent) {
        let blob = new Blob([xhr.response], {type: 'application/vnd.ms-excel'});
        if (blob.size === 0) {
            alert('Отчетных дней не найдено. Смените фильтры поиска!')
            return
        }
        let link = document.createElement('a');
        link.download = 'Report.xls';
        link.href = URL.createObjectURL(blob);
        link.click();
        URL.revokeObjectURL(link.href);
    };
    xhr.send(data)
}