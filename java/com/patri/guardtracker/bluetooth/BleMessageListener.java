package com.patri.guardtracker.bluetooth;

/**
 * Created by patri on 10/11/2016.
 */
public interface BleMessageListener {
    public void onMessageReceived(byte[] msgRecv);
    // Para já, não vejo a necessidade do método onMessageSent porque a chamada deste método não é assíncrona em relação à execução do método para enviar uma mensagem (writeBytes).
    // Portanto, quem executa writBytes sabe que logo a seguir seria executado o onMessageSent
//    public void onMessageSent(byte [] msgSent);
}
