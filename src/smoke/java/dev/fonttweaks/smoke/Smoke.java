package dev.fonttweaks.smoke;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
/** Isolated Fabric preLaunch test: exits before any game window/world is created. */
public final class Smoke implements PreLaunchEntrypoint {
 public void onPreLaunch() {
  String[] names={"net.minecraft.client.gui.Font","net.minecraft.client.gui.Font$PreparedTextBuilder","net.minecraft.client.renderer.state.gui.GuiTextRenderState","net.minecraft.client.gui.GuiGraphicsExtractor","net.minecraft.client.gui.components.ChatComponent","net.minecraft.client.gui.components.MultiLineLabel$2","net.minecraft.client.gui.components.MultiLineTextWidget","net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip"};
  try {
   for(String name:names) {
    Class<?> c=Class.forName(name,false,Thread.currentThread().getContextClassLoader());
    long count=java.util.Arrays.stream(c.getDeclaredMethods()).filter(m->m.getName().contains("fonttweaks")).count();
    if(count==0) throw new AssertionError("No FontTweaks hooks: "+name);
    System.out.println("FONT_SMOKE_OK "+name+" hooks="+count);
   }
   spacingProbe();
   System.out.println("FONT_SMOKE_COMPLETE");
   System.exit(0);
  } catch(Throwable e) {e.printStackTrace();System.exit(12);}
 }
 static void spacingProbe() throws Exception {
  ClassLoader loader=Thread.currentThread().getContextClassLoader();
  Class<?> settings=Class.forName("dev.sanowo.fonttweaks.FontTweaksSettings",true,loader);
  Object cfg=settings.getMethod("get").invoke(null);
  java.lang.reflect.Field factor=settings.getField("globalLineSpacing");
  // Save/reload through the real mod config in the isolated run-smoke directory.
  factor.setDouble(cfg,2);
  settings.getMethod("save").invoke(null);
  settings.getMethod("load").invoke(null);
  cfg=settings.getMethod("get").invoke(null);
  if(factor.getDouble(cfg)!=2)throw new AssertionError("global spacing config reload failed");
  java.lang.reflect.Field f=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
  f.setAccessible(true);Object unsafe=f.get(null);
  java.lang.reflect.Method allocate=unsafe.getClass().getMethod("allocateInstance",Class.class);
  Class<?> sequence=Class.forName("net.minecraft.util.FormattedCharSequence",true,loader);
  Object seq=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{sequence},(p,m,a)->true);
  Class<?> language=Class.forName("net.minecraft.locale.Language",true,loader);
  Object lang=language.getMethod("getInstance").invoke(null);
  Class<?> labelType=Class.forName("net.minecraft.client.gui.components.MultiLineLabel$2",true,loader);
  Object label=allocate.invoke(unsafe,labelType);
  Class<?> rowType=Class.forName("net.minecraft.client.gui.components.MultiLineLabel$TextAndWidth",true,loader);
  java.lang.reflect.Constructor<?> ctor=rowType.getDeclaredConstructors()[0];ctor.setAccessible(true);
  Object row=ctor.newInstance(seq,6);
  java.lang.reflect.Field rows=labelType.getDeclaredField("cachedTextAndWidth");rows.setAccessible(true);rows.set(label,java.util.List.of(row,row));
  java.lang.reflect.Field langField=labelType.getDeclaredField("splitWithLanguage");langField.setAccessible(true);langField.set(label,lang);
  Class<?> alignment=Class.forName("net.minecraft.client.gui.TextAlignment",true,loader);
  Object left=alignment.getField("LEFT").get(null);
  Class<?> collectorType=Class.forName("net.minecraft.client.gui.ActiveTextCollector",true,loader);
  java.util.List<Integer> ys=new java.util.ArrayList<>();
  Object collector=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{collectorType},(p,m,a)->{
   if(m.getName().equals("accept")&&a.length==3)ys.add((Integer)a[1]);return null;
  });
  java.lang.reflect.Method visit=labelType.getDeclaredMethod("visitLines",alignment,int.class,int.class,int.class,collectorType);visit.setAccessible(true);
  factor.setDouble(cfg,1);visit.invoke(label,left,0,10,9,collector);
  if(!ys.equals(java.util.List.of(10,19)))throw new AssertionError("baseline="+ys);
  ys.clear();factor.setDouble(cfg,2);visit.invoke(label,left,0,10,9,collector);
  if(!ys.equals(java.util.List.of(10,28)))throw new AssertionError("scaled="+ys);
  Class<?> fontType=Class.forName("net.minecraft.client.gui.Font",true,loader);
  Object font=allocate.invoke(unsafe,fontType);
  Class<?> tooltipType=Class.forName("net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip",true,loader);
  Object tip=tooltipType.getConstructor(sequence).newInstance(seq);
  java.lang.reflect.Method height=tooltipType.getMethod("getHeight",fontType);
  factor.setDouble(cfg,1);int normal=(Integer)height.invoke(tip,font);
  factor.setDouble(cfg,2);int scaled=(Integer)height.invoke(tip,font);
  if(scaled!=normal*2)throw new AssertionError("tooltip "+normal+" -> "+scaled);
  factor.setDouble(cfg,1);
  System.out.println("FONT_SPACING_SMOKE labels=[10,19] -> [10,28]; tooltip="+normal+" -> "+scaled);
 }
}
