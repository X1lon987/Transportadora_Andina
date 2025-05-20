export default class Mercancias {
  static #table
  static #modal
  static #currentOption
  static #form
  static #customers
  
  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Mercancias.init()')
  }

  static async init() {
    try {     
      Mercancias.#form = await Helpers.fetchText('./resources/html/mercancias.html') //Aqui estamos cargando un formulario externo
      // intentar cargar los datos de los usuarios
      let response = await Helpers.fetchJSON(`${urlAPI}/cliente`)  //Aqui cargamos la informacion de los clientes
      if (response.message != 'ok') {
        throw new Exception(response) // JavaScript no cuenta con una clase Exception
      }

  // crear las opciones para un select de clientes
  //si la informacion de los clientes llega sin problemas usamos este metodo que se encarga de crear unas opciones especiales para los clientes que se utilizaran mas adelante
      Mercancias.#customers =  Helpers.toOptionList({ 
        items: response.data,
        value: 'id',
        text: 'nombre',
        firstOption: 'Seleccione cliente',
      })

//Validacion de la carga de mercancias 
      response = await Helpers.fetchJSON(`${urlAPI}/mercancia`)
      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
      <div class="p-2 w-full">
          <div id="table-container" class="m-2"></div>
      </dv>`

      Mercancias.#table = new Tabulator('#table-container', {
        height: tableHeight, // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data,
        layout: 'fitColumns', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Mercancias.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Mercancias.#deleteRowClick },
          { title: 'Id', field: 'id', hozAlign: 'center', width: 90 },
          { title: 'Cliente', field: 'cliente.nombre', width: 200 },
          { title: 'Dice contener', field: 'contenido', width: 300 },
          { title: 'Ingreso', field: 'fechaEntrada', width: 150, formatter: 'datetime', formatterParams: formatDateTime },
          { title: 'Salida', field: 'fechaSalida', width: 150, formatter: 'datetime', formatterParams: formatDateTime },
          { title: 'Días', field: 'calcularDias', hozAlign: 'center', width: 65 },
          { title: 'Alto', field: 'volumen', hozAlign: 'center', visible: false },
          { title: 'Ancho', field: 'volumen', hozAlign: 'center', visible: false },
          { title: 'Largo', field: 'volumen', hozAlign: 'center', visible: false },
          { title: 'Vol. m³', field: 'volumen', hozAlign: 'center', width: 80, formatterParams: {precision : 0} },
          { title: 'Costo', field: 'costo', hozAlign: 'right', width: 100, formatter: 'money', formatterParams:{precision:0} },
          { title: 'Bodega', field: 'bodega', width: 280 },
        ],
        responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
        initialSort: [
          // establecer el ordenamiento inicial de los datos
          { column: 'fechaEntrada', dir: 'asc' },
        ],
        columnDefaults: {
          tooltip: true, //show tool tips on cells
        },
        // mostrar al final de la tabla un botón para agregar registros
        footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
      })
      Mercancias.#table.on('tableBuilt', () =>
        document.querySelector('#add-row').addEventListener('click', Mercancias.#addRow)
      )

    } catch (e) {
      Toast.show({ title: 'Mercancias', message: 'Falló la carga de la información', mode: 'danger', error: e })
    }
    return this
  }

  static async #addRow() {
    Mercancias.#currentOption = 'add'
    Mercancias.#modal = new Modal({  //Crea una instancia de un objeto Popup el cual nos permite crear ventanas adyacentes o desplegables en nuestra pagina web
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      //classes: es una propiedad del objeto modal que acabamos de crear que le da estilos al bootstrap
      title: `<h5 class="text-secondary">Ingreso de Mercancias</h5>`,                //Titulo del cuadro de dialogo donde se ingresaran las mercancias
      content: Mercancias.#form,  //content: aqui se almacena el cuerpo de la ventana modal donde se van a ingresar las mercancias
      buttons: [                   //recibe un array en que va a ir implementada la funcion de lso botones que van a aparecer dentro de la ventana modal
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Mercancias.#add() },  //Utilizamos un callBack para no estar cargando siempre una fumcion que solo vamos a utilizar cuando se abra la ventana modal
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Mercancias.#modal.close() }   //este boton va a tener la funcion  de cerrar la ventana modal
      ],
      doSomething: Mercancias.#displayDataOnForm  //esta instruccion nos muestra la ventana modal que acabamos de personalizar
    })
    Mercancias.#modal.show()
  }
  
  static #editRowClick = async (e, cell) => { //cell nos referencia sobre la fila en la que estamos trabajando o sobre la cual estamos ubicados 
    Mercancias.#currentOption = 'edit'
    console.log(cell.getRow().getData())

    //Vamos a implementar una nueva ventana modal para poder ingresar los combios que queremos hacer para las mercancias
    Mercancias.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class= "text-secondary">Actualizacion de Mercancias</h5>`,
      content: Mercancias.#form,
      buttons: [
        { caption: editButton, classes: 'btn btn-primary me-2', action: () => Mercancias.#edit(cell) },   //como estre metodo toma una cell especifica que es aquella que se va a editar por eso la referenciamos dentro de la funcion edit
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Mercancias.#modal.close() },
      ],
      doSomething: (idModal) => Mercancias.#displayDataOnForm(idModal, cell.getRow().getData()), //este llamado al metodo le enviamos el idModal y la informacion almacenada dentro de la fila en la que se esta situado mediante getRow()
    })
    Mercancias.#modal.show()
  }

  static #deleteRowClick = async (e, cell) => {
    Mercancias.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    //Mismo proceso, la idea es mostrar un cuadro de dialogo
    Mercancias.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5 class= "text-secondary">Eliminar Mercancias</h5>`,
      content: `<span class= "text-back dark:text-gray-300">
                  Confirme la eliminacion de la mercancia: <br>
                  Id:${cell.getRow().getData().id} - Contenido:${cell.getRow().getData().contenido}<br>         
                  Bodega : ${cell.getRow().getData().bodega}<br>
                  Propietario : ${cell.getRow().getData().cliente.nombre}<br>
                  </span>`,//en content obtuvimos la informacion de la mercancia mediante el cell para mostrar en el cuadro de dialogo sobre la mercancia a eliminar
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Mercancias.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Mercancias.#modal.close() },
      ],
    })
    Mercancias.#modal.show()
  }
  //agregar nuevas mercancias(creacion y personalizacion de una ventana modal al presionar  nuevo registro)

  static async #add() {//proximamnete este metodo se encargara de hacer peticiones tipo post
    try {
      if(!Helpers.okForm('#form-mercancias')){
        return 
      }
      const body = Mercancias.#getFormData()

      let response = await Helpers.fetchJSON(`${urlAPI}/mercancia`,{
        method: 'POST',
        body,
      })
      if(response.message === 'ok'){
        Mercancias.#table.addRow(response.data)
        Mercancias.#modal.remove()
        Toast.show({message: 'Agregado exitosamente'})
      }else{
        Toast.show({message: 'no se pudo agregar el registro', mode: 'danger', error: response}) 
      }
      
    } catch (e) {
      Toast.show({message: 'Fallo la operacion de la creacion del registro', mode: 'danger', error: e})      
    }
  }

  static async #edit(cell) {//Proximamente este metodo se encargara de hacer las peticiones patch
    try {
      if(!Helpers.okForm('#form-mercancias')){
        return
      }
      const body = Mercancias.#getFormData()
      const url = `${urlAPI}/mercancia/${cell.getRow().getData().id}`   //Aqui estamos guardando la direccion o end point especifico de la mercancias que deseamos editar

      let response = await Helpers.fetchJSON(url,{
        method: 'PATCH',
        body,
      })
      if(response.message==='ok'){
        Toast.show({message: 'la mercancia  fue actualizada exitosamente!'})
        cell.getRow().update(response.data)
        Mercancias.#modal.remove()
      }
      else{
        Toast.show({message: 'No se pudo actualizar la mercancias',mode: 'danger', error: response})
      }
    } catch (e) {
      Toast.show({message: 'Problemas al actualizar mercancias (en el try)', mode: 'danger', error: e})
    }
  }

  static async #delete(cell) {//Proximamente este metodo se encargara de hacer la peticion delete 
    try {
      
      const url = `${urlAPI}/mercancia/${cell.getRow().getData().id}`
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
        //Al borrar no necesitamos cargas de ningun tipo, por eso este metodo no contiene body
      })
      if(response.message === 'ok'){
        Toast.show({message: 'la mercancia ha sido eliminada de manera exitosa!'})
        cell.getRow().delete()
        Mercancias.#modal.close()
      }else{
        Toast.show({message: 'No se pudo eliminar la mercancia', mode: 'danger', error: response})
      }

    } catch (e) {
      Toast.show({message: 'Problemas al eliminar la mercancia (try)',mode:'danger',error: e})
    }
  }

  static #displayDataOnForm(idModal, rowData) {//Este metodo se encarga de mostrar y editar el registro de mercancias de la ventana modal
    // referenciar el select "cliente"
    const selectCustomers = document.querySelector(`#${idModal} #cliente`)

    // asignar la lista de opciones al select "cliente" de mercancias.html
    selectCustomers.innerHTML = Mercancias.#customers

    if(Mercancias.#currentOption === 'edit'){
      //Mostrar todos los datos de la fila actual en el formulario
      document.querySelector(`#${idModal} #id`).value = rowData.id
      document.querySelector(`#${idModal} #contenido`).value = rowData.contenido
      document.querySelector(`#${idModal} #alto`).value = rowData.alto
      document.querySelector(`#${idModal} #ancho`).value = rowData.ancho
      document.querySelector(`#${idModal} #largo`).value = rowData.largo
      document.querySelector(`#${idModal} #ingreso`).value = rowData.fechaEntrada //el #ingreso corresponde dentro del mercancias html al campo (id:)
      document.querySelector(`#${idModal} #salida`).value = rowData.fechaSalida
      document.querySelector(`#${idModal} #bodega`).value = rowData.bodega
      selectCustomers.value = rowData.cliente.id
      
    }else{
      const now = DateTime.now() ///Instruccion de luxon que permite o devuelve la fecha y la hora actual      
      document.querySelector('#form-mercancias #ingreso').value = now.toFormat('yyyy-MM-dd HH:mm')
      //por defecto se le asigna a la hora de salida una hora mas que a la de entrada
      document.querySelector('#form-mercancias #salida').value = now.plus({ hours: 1}).toFormat('yyyy-MM-dd HH:mm') //el now.plus() nos permite sumar una hora, minuto o segundo segun se asigne 
    }
  }

  static #getFormData(){
    return{
      id: document.querySelector(`#${Mercancias.#modal.id} #id`).value,
      cliente: document.querySelector(`#${Mercancias.#modal.id} #cliente`).value,
      contenido: document.querySelector(`#${Mercancias.#modal.id} #contenido`).value,
      alto: parseFloat(document.querySelector(`#${Mercancias.#modal.id} #alto`).value),
      ancho: parseFloat(document.querySelector(`#${Mercancias.#modal.id} #ancho`).value),
      largo: parseFloat(document.querySelector(`#${Mercancias.#modal.id} #largo`).value),
      fechaEntrada: document.querySelector(`#${Mercancias.#modal.id} #ingreso`).value,
      fechaSalida: document.querySelector(`#${Mercancias.#modal.id} #salida`).value,
      bodega: document.querySelector(`#${Mercancias.#modal.id} #bodega`).value
    }
  }
} 
