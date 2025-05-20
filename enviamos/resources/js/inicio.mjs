
export default class Inicio{
    static #info
    static async init(){
        Inicio.#info = await Helpers.fetchText('./resources/html/inicio.html')
        document.querySelector('main').innerHTML = Inicio.#info
    }
}