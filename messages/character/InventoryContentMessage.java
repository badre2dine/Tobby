package messages.character;

import gamedata.inventory.ObjectItem;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class InventoryContentMessage extends Message {
    public Vector<ObjectItem> objects;
    public int kamas = 0;
    
    public InventoryContentMessage(Message msg) {
    	super(msg);
    	this.objects = new Vector<ObjectItem>();
    	deserialize();
    }
    
    private void deserialize() {
    	ByteArray buffer = new ByteArray(this.content);
    	int nb = buffer.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.objects.add(new ObjectItem(buffer));
    	this.kamas = buffer.readVarInt();
    }
}