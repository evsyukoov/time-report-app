
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
    alert(dep + " " + empl + " " + dateStart + " " + dateEnd)
    var formData = new FormData()
    formData.append('name', empl)
    formData.append('department', dep)

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "http://localhost:8080/report/get-report");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    var data = JSON.stringify({ "name":
        empl, "department": dep, "dateStart": dateStart, "dateEnd": dateEnd });
    xhr.send(data)
}