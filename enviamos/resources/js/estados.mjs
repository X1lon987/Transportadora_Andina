
export default class Estados {
    static #tipo
    static #numGuia
    static #selectEstados
    static #modal
    static #table
    static #formPrincipal
    static #formAgregar
    static #estadosActuales

    constructor() {
        throw new Error('no requiere')
    }

    static async init() {
        try {

            Estados.#formPrincipal = await Helpers.fetchText(`./resources/html/estados.html`)
            //Agregar al html principal el form de estados
            Estados.#formAgregar = await Helpers.fetchText(`./resources/html/formEstados.html`)

            document.querySelector('main').innerHTML = Estados.#formPrincipal

            //---------------------------------------Mostrar la hora cada segundo actualizada--------------------------------------------------------

            //hay que usar una comprobacion para verificar que el elemento del formulario este cargado para que asi al cambiar de seccion de la pagina no se siga cargando
            const element = document.querySelector('#form-estados #fechaHora')
            element.value = DateTime.now().setLocale('es-CO').toFormat('yyyy-MM-dd a HH:mm:ss')

            element ? setInterval(() => (element.value = DateTime.now().setLocale('es-CO').toFormat('yyyy-MM-dd a HH:mm:ss')), 1000) : ''

            //--------------------------------------Evaluar el evento del tipo de envio que se desea consultar---------------------------------

            //el error parte de aqui ya que no se como hacer que me cargue dentro del main esa tabla
            document.querySelector('#form-estados #buscar').addEventListener('click', async e => {//Aca estara esperanod a que el evento sobre alguno de las opciones sea un click
                e.preventDefault()

                //evaluar o almacenar el numGuia ingresado por el ususario
                Estados.#tipo = document.querySelector('#form-estados #tipoEnvio').value
                Estados.#numGuia = document.querySelector('#form-estados #numGuia').value.toUpperCase()

                if (!Helpers.okForm('#form-estados')) {
                    return
                }
                //Aqui se hace la carga respectiva de los paquetes
                let response = await Helpers.fetchJSON(`${urlAPI}/${Estados.#tipo}/id/${Estados.#numGuia}`)
                document.querySelector('#div-info').style.display = ''

                if (response.message !== 'ok') {
                    document.querySelector('#div-info').innerHTML = `<h4>Informacion Del Envio</h4>
                    <div class="alert alert-warning" rol="alert">
                    <h5 class="alert-heading">Busqueda Fallida</h5>
                    <p>${response.message}</p>
                    </div>`
                }
                else {
                    let infoEnvio = response.data
                    document.querySelector('#div-info').innerHTML = `<div id="div-info-envio" class="ms-2 mb-1 "></div> 
                    <div id="tabla"></div>`
                    document.querySelector('#div-info-envio').innerHTML = `<h4>Informacion Del Envio</h4>
                    <span type="text" class="text-dark">
                    Remitente: ${infoEnvio.remitente.nombre} - ${infoEnvio.remitente.direccion} - ${infoEnvio.remitente.ciudad}<br>
                    Destinatario: ${infoEnvio.destinatario.nombre} - ${infoEnvio.destinatario.direccion} - ${infoEnvio.destinatario.ciudad}<br>
                    Dice Contener: ${infoEnvio.contenido}<br>
                    Valor Envio: ${infoEnvio.costo}</span>
                    `
                    Estados.#crearTabla(response)
                    Estados.#estadosActuales = response.data.estados

                    let estados = await Helpers.fetchJSON(`${urlAPI}/envio/estados`)
                    console.log(estados)
                    Estados.#selectEstados = Helpers.toOptionList({
                        items: estados.data,
                        value: 'key',
                        text: 'value',
                        firstOption: 'Seleccione Un Estado',
                    })
                }

            })
        } catch (e) {
            Toast.show({ title: 'Estados', message: 'fallo en la carga de la informacion (try)', mode: 'warning' })
        }
    }
    static #crearTabla(response) {
        Estados.#table = new Tabulator('#tabla', {

            // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
            data: response.data.estados, //Aqui se carga la informacion de los Cajas
            layout: 'fitColumns', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
            columns: [
                // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
                { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Estados.#deleteRowClick },
                {
                    title: 'Hora y Fecha',
                    field: 'fechayHora',
                    width: 472,
                    formatter: function (cell) {
                        const time = cell.getValue()
                        return DateTime.fromISO(time).toFormat('hh:mm a - cccc dd LLLL yyyy')
                    }
                },
                { title: 'Estado', field: 'estado', width: 500 },
            ],
            responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
            columnDefaults: {
                tooltip: true, //show tool tips on cells
            },
            footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
        })
        Estados.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Estados.#addRow))
    }

    static #deleteRowClick = async (e, cell) => {
        let fecha = cell.getRow().getData().fechayHora
        let format = DateTime.fromISO(fecha).toFormat('yy-MM-dd a hh:mm')
        //Mismo proceso, la idea es mostrar un cuadro de dialogo
        Estados.#modal = new Modal({
            classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
            title: `<h5 class="text-secondary">Eliminar Estados</h5>`,
            content: `<span class= "text-back dark:text-gray-300">
                          Confirme la eliminacion del Estado: <br>
                          <br>
                          <strong>Estado:</strong>${cell.getRow().getData().estado}<br> 
                          <strong>Fecha Y Hora:</strong> ${format}           
                          </span>`,
            buttons: [ //preguntar como hacer que aparezca el estado actual del envio
                { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Estados.#delete(cell) },
                { caption: cancelButton, classes: 'btn btn-secondary', action: () => Estados.#modal.remove() },
            ],
        })
        Estados.#modal.show()
    }

    static #addRow() {
        Estados.#modal = new Modal({
            classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
            title: `<h5 class="text-secondary">Adicion de estados</h5>`,
            content: Estados.#formAgregar,
            buttons: [
                { caption: addButton, classes: 'btn btn-primary me-2', action: () => Estados.#add() },  //Utilizamos un callBack para no estar cargando siempre una fumcion que solo vamos a utilizar cuando se abra la ventana modal
                { caption: cancelButton, classes: 'btn btn-secondary', action: () => Estados.#modal.remove() }
            ],
            doSomething: (idModal) => Estados.#displayDataOnForm(idModal)
        })
        Estados.#modal.show()
    }
    static async #delete(cell) {
        const url = `${urlAPI}/${Estados.#tipo}/${Estados.#numGuia}`
        
        
        if(Estados.#estadosActuales.includes("ENTREGADO") || Estados.#estadosActuales.includes("DEVUELTO")){
            Toast.show({message: 'No se pueden eliminar estados despues de ser ENTREGADO O DEVUELTO el pedido',mode:'warning'})   
        }
        
        let ultimo = Estados.#estadosActuales.length -1 //Sacamos el indice maximo del arreglo de los estados que tenemos dentro de nuestra tabla
        if(cell.getRow().getData()!== Estados.#estadosActuales[ultimo]){ //Verificamos si la informacion de la data coincide o no con la del ultimo estado, de ser asi se deja eliminar, sino, no
            Toast.show({message: 'No se puede eliminar un estado que no sea el ultimo (se tira el orden Logico)',mode: 'warning'})
        }else{
            let backUp = Estados.#estadosActuales
            cell.getRow().delete()
            const body = {estados: Estados.#table.getData()}
            let response = await Helpers.fetchJSON(url,{
                method: 'PATCH',
                body,
            })
            if((response.message=='ok')){
                Toast.show({message: 'El estado fue eliminado con exito'})
                Estados.#table.replaceData(response.data.estados)
                Estados.#estadosActuales = response.data.estados
                Estados.#modal.remove()
            }else[
                Toast.show({message: response.message, mode: 'warning'})
            ]
        }
    }
    static async #add() {
        try {
            if(!Helpers.okForm('#form-agregar', Estados.#otherValidations)){
                return
            }
            let nuevoEstado =  Estados.#getFormData()
            const estadoBackUp = Estados.#estadosActuales 
            console.log(Estados.#estadosActuales)           
            Estados.#estadosActuales.push(nuevoEstado)

            let body = {estados: Estados.#estadosActuales}
            const url = `${urlAPI}/${Estados.#tipo}/${Estados.#numGuia}`
            console.log(body)
            let response = await Helpers.fetchJSON(url, {
                method: 'PATCH',
                body,
            })
            if (response.message === 'ok') {
                Toast.show({ message: 'Agregado Exitosamente' })
                Estados.#table.replaceData(response.data.estados)
                Estados.#modal.remove()
                
            }else{
                Toast.show({message: response.message, mode: 'warning'})
                Estados.#estadosActuales.pop() //Se utiliza el metodo pop() para elimianr el ultimo elemento agregado ya que si entro a esta validacion significa que no funciono
            }
        } catch (e) {

        }
    }
    static #displayDataOnForm(idModal) {
        const now = DateTime.now().toFormat('yyyy-MM-dd HH:mm:ss')
        console.log(now)
        
        document.querySelector(`#${idModal} #estado`).innerHTML = Estados.#selectEstados
        document.querySelector(`#${idModal} #fechayHora`).value = now
        document.querySelector(`#${idModal} #fechayHora`).disabled = true
            
    }
    static #getFormData(){
        return{
            estado: document.querySelector(`#${Estados.#modal.id} #estado`).value,
            fechayHora: document.querySelector(`#${Estados.#modal.id} #fechayHora`).value
        }
    }
    static #otherValidations(){
        let respuesta = true
        const selectEstado = document.querySelector(`#${Estados.#modal.id} #estado`).value

        if(selectEstado ===''){
            Toast.show({message: 'Falta seleccionar un estado',mode:'warning'})
            respuesta = false
        }
        
        return respuesta
    }

}