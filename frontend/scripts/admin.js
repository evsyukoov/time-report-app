function fillSelectSections(idElem) {
    const select = document.getElementById(idElem)
    let optionSize = select.length
    if (optionSize > 1) {
        return
    }
    let filledArray
    if (idElem.startsWith('employees-admin')) {
        filledArray = employees.filter(empl => empl['actual'] == false).map(empl => empl['employeeName'])
    } else if (idElem.startsWith('departments-admin')) {
        filledArray = departments.map(dep => dep['name'])
    } else if (idElem.startsWith('projects-admin')) {
        filledArray = projects.filter(proj => proj['used'] == false).map(proj => proj['projectName'])
    } else if (idElem.startsWith('employees-archive-admin')) {
        filledArray = employees.filter(empl => empl['actual'] == true && empl['archived'] == false).map(empl => empl['employeeName'])
    } else if (idElem.startsWith('projects-archive-admin')) {
        filledArray = projects.filter(proj => proj['used'] == true && proj['archived'] == false).map(proj => proj['projectName'])
    }
    for (let i = 0; i < filledArray.length; i++) {
        let option = document.createElement('option')
        option.value = i + ''
        option.innerHTML = filledArray[i]
        option.text = filledArray[i]
        select.appendChild(option)
    }
}

function addEmployee() {
    let position = document.getElementById("employee-position").value
    let fio = document.getElementById("employee-fio").value
    let selectList = document.getElementById("departments-admin");
    let department = selectList.options[selectList.selectedIndex].text
    if (!getDepartmentsNames().includes(department)) {
        swal("Выберите отдел!", {
            icon: "error",
        });
        return;
    }
    if (position == null || position === "" || position.length < 5) {
        swal("Введите валидное поле должность!", {
            icon: "error",
        });
        return;
    }
    if (fio == null || fio === "") {
        swal("Поле ФИО не может быть пустым!", {
            icon: "error",
        });
        return;
    }
    let fioArr = fio.split(" ")
    if (fioArr.length !== 3) {
        swal("Введите Фамилию имя и отчество", {
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("POST", referer + "/time-report-app/admin/employee/add");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"name": fio, "position": position, "department": department, "departmentShort": getShortNameByName(department)});

    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                swal("Новый сотрудник успешно добавлен", {
                    icon: "success",
                });
            } else if (xhr.status === 400) {
                if (xhr.responseText.includes('ALREADY_CONTAINS')) {
                    swal("Такой сотрудник уже есть в справочнике!", {
                        icon: "error",
                    });
                } else {
                    swal("Проверьте введеные данные по сотруднику!", {
                        icon: "error",
                    });
                }
            } else if (xhr.status === 500) {
                swal("Произошла непредвиденная ошибка на сервере, сообщите администратору", {
                    icon: "error",
                });
            }
        }
    };
    xhr.send(data)
}

function addProject() {
    let project = document.getElementById("project-input").value
    if (project == null || project === "" || project.length < 5) {
        swal("Введите валидное поле проект!", {
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("POST", referer + "/time-report-app/admin/project/add");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"projectName": project});

    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                swal("Новый проект успешно добавлен", {
                    icon: "success",
                });
            } else if (xhr.status === 400) {
                if (xhr.responseText.includes('ALREADY_CONTAINS')) {
                    swal("Такой проект уже есть в справочнике!", {
                        icon: "error",
                    });
                } else {
                    swal("Проверьте введеные данные по проекту!", {
                        icon: "error",
                    });
                }
            } else if (xhr.status === 500) {
                swal("Произошла непредвиденная ошибка на сервере, сообщите администратору", {
                    icon: "error",
                });
            }
        }
    };
    xhr.send(data)
}

function rmEmployee() {
    let selectList = document.getElementById("employees-admin");
    let employee = selectList.options[selectList.selectedIndex].text
    let employeeNames = employees.map(e => e['employeeName'])
    if (!employeeNames.includes(employee)) {
        swal("Выберите сотрудника!", {
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("DELETE", referer + "/time-report-app/admin/employee/remove");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"name": employee});

    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                swal("Сотрудник успешно удален из справочника", {
                    icon: "success",
                });
                let select = document.getElementById("employees-admin")
                select.removeChild(selectList.options[selectList.selectedIndex])
            } else if (xhr.status === 400) {
                if (xhr.responseText.includes('IMPOSSIBLE_DELETE')) {
                    swal("Невозможно удалить сотрудника у которого есть отчеты. Обратитесь к администратору", {
                        icon: "error",
                    });
                } else {
                    swal("Вы не выбрали сотрудника!", {
                        icon: "error",
                    });
                }
            } else if (xhr.status === 500) {
                swal("Произошла непредвиденная ошибка на сервере, сообщите администратору", {
                    icon: "error",
                });
            }
        }
    };
    xhr.send(data)
}

function rmProject() {
    let selectList = document.getElementById("projects-admin");
    let project = selectList.options[selectList.selectedIndex].text
    let projectNames = projects.map(p => p['projectName'])
    if (!projectNames.includes(project)) {
        swal("Выберите проект!", {
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("DELETE", referer + "/time-report-app/admin/project/remove");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"projectName": project});

    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                swal("Проект успешно удален из справочника", {
                    icon: "success",
                });
                let select = document.getElementById("projects-admin")
                select.removeChild(selectList.options[selectList.selectedIndex])
            } else if (xhr.status === 400) {
                if (xhr.responseText.includes('IMPOSSIBLE_DELETE')) {
                    swal("Невозможно удалить проект, на который ссылаются отчеты сотрудников. Обратитесь к администратору", {
                        icon: "error",
                    });
                } else {
                    swal("Вы не выбрали проект!", {
                        icon: "error",
                    });
                }
            } else if (xhr.status === 500) {
                swal("Произошла непредвиденная ошибка на сервере, сообщите администратору", {
                    icon: "error",
                });
            }
        }
    };
    xhr.send(data)
}

function archiveEmployee() {
    let selectList = document.getElementById("employees-archive-admin");
    let employee = selectList.options[selectList.selectedIndex].text
    let employeeNames = employees.map(e => e['employeeName'])
    if (!employeeNames.includes(employee)) {
        swal("Выберите сотрудника!", {
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("PATCH", referer + "/time-report-app/admin/employee/archive");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"name": employee});

    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                swal("Сотрудник успешно перемещен в архив из справочника", {
                    icon: "success",
                });
                let select = document.getElementById("employees-archive-admin")
                select.removeChild(selectList.options[selectList.selectedIndex])
            } else if (xhr.status === 400) {
                if (xhr.responseText.includes('NOT_FOUND')) {
                    swal("Не найден сотрудник. Обратитесь к администратору", {
                        icon: "error",
                    });
                } else {
                    swal("Вы не выбрали сотрудника!", {
                        icon: "error",
                    });
                }
            } else if (xhr.status === 500) {
                swal("Произошла непредвиденная ошибка на сервере, сообщите администратору", {
                    icon: "error",
                });
            }
        }
    };
    xhr.send(data)
}

function archiveProject() {
    let selectList = document.getElementById("projects-archive-admin");
    let project = selectList.options[selectList.selectedIndex].text
    let projectNames = projects.map(p => p['projectName'])
    if (!projectNames.includes(project)) {
        swal("Выберите проект!", {
            icon: "error",
        });
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("PATCH", referer + "/time-report-app/admin/project/archive");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
    xhr.withCredentials = true
    const data = JSON.stringify({"projectName": project});

    xhr.onreadystatechange = function (oEvent) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                swal("Проект успешно перемещен в архив из справочника", {
                    icon: "success",
                });
                let select = document.getElementById("projects-archive-admin")
                select.removeChild(selectList.options[selectList.selectedIndex])
            } else if (xhr.status === 400) {
                if (xhr.responseText.includes('IMPOSSIBLE_DELETE')) {
                    swal("Не найден проект. Обратитесь к администратору", {
                        icon: "error",
                    });
                } else {
                    swal("Вы не выбрали проект!", {
                        icon: "error",
                    });
                }
            } else if (xhr.status === 500) {
                swal("Произошла непредвиденная ошибка на сервере, сообщите администратору", {
                    icon: "error",
                });
            }
        }
    };
    xhr.send(data)
}

function getDepartmentsNames() {
    let departmentsNames = []
    for (let i = 0; i < departments.length; i++) {
        departmentsNames.push(departments[i]["name"]);
    }
    return departmentsNames;
}

function getShortNameByName(name) {
    for (let i = 0; i < departments.length; i++) {
        if (departments[i]["name"] === name) {
            return departments[i]["shortName"];
        }
    }
}


