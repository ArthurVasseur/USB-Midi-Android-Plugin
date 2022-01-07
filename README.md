# USB Midi Android Plugin

USB Midi Android Plugin is a plugin for communicate with a Midi device through USB.

## Usage

#### Java Android


In the first place you have to inherits from `IMidiCallback` interface.

```java
public class MyCallback implements IMidiCallback {
    @Override
    public void NoteOn(int note, int velocity) {
        Log.i("MyCallback", "NoteOn");
    }

    @Override
    public void NoteOff(int note) {
        Log.i("MyCallback", "NoteOff");
    }

    @Override
    public void DeviceAttached(String name) {
        Log.i("MyCallback", "DeviceAttached");
    }

    @Override
    public void DeviceDetached(String name) {
        Log.i("MyCallback", "DeviceDetached");
    }
}
```

Then you have to call the method called `ctor` :

```java
public void randomFunction()
{
    UsbMidiController instance = UsbMidiController.getInstance();
    MyCallback myCallback = new MyCallback();
    instance.ctor(myCallback, androidActivity);
}
```
Now you should be able to receive Midi events.

## License

[MIT](https://choosealicense.com/licenses/mit/)
