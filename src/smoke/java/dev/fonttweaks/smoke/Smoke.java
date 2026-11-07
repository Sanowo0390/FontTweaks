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
   glyphVariantProbe();
   System.out.println("FONT_SMOKE_COMPLETE");
   System.exit(0);
  } catch(Throwable e) {e.printStackTrace();System.exit(12);}
 }
 static void glyphVariantProbe() throws Exception {
  ClassLoader loader=Thread.currentThread().getContextClassLoader();
  Class<?> delayed=Class.forName("net.minecraft.client.gui.font.FontSet$DelayedBake",true,loader);
  Class<?> glyphType=Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$Glyph",true,loader);
  if(java.util.Arrays.stream(delayed.getDeclaredMethods()).noneMatch(m->m.getName().contains("fonttweaks")))
   throw new AssertionError("Missing variant cache hook");
  Class<?> marker=Class.forName("dev.sanowo.fonttweaks.UnifontGlyph",true,loader);
  Class<?> lines=Class.forName("net.minecraft.client.gui.font.providers.UnihexProvider$LineData",true,loader);
  Object contents=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{lines},(p,m,a)->
      m.getName().equals("bitWidth") ? 16 : 0x80000000);
  java.lang.reflect.Constructor<?> glyphCtor=glyphType.getDeclaredConstructors()[0];glyphCtor.setAccessible(true);
  Object glyph=glyphCtor.newInstance(contents,0,15);
  if(!marker.isInstance(glyph))throw new AssertionError("Unihex marker missing");
  Class<?> settings=Class.forName("dev.sanowo.fonttweaks.FontTweaksSettings",true,loader);
  Object cfg=settings.getMethod("get").invoke(null);
  settings.getField("guiScaleProfiles").set(cfg,new java.util.LinkedHashMap<>());
  java.lang.reflect.Field enabled=settings.getField("pixelationEnabled"), pixels=settings.getField("pixelBlockSize");
  Class<?> fontSet=Class.forName("net.minecraft.client.gui.font.FontSet",true,loader);
  java.lang.reflect.Field unsafeField=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
  unsafeField.setAccessible(true);Object unsafe=unsafeField.get(null);
  Object owner=unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,fontSet);
  Class<?> stitcher=Class.forName("com.mojang.blaze3d.font.UnbakedGlyph$Stitcher",true,loader);
  Class<?> baked=Class.forName("net.minecraft.client.gui.font.glyphs.BakedGlyph",true,loader);
  int[] bakes={0};
  Object fakeStitcher=java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{stitcher},(p,m,a)->{
   if(!m.getName().equals("stitch"))return null;
   bakes[0]++;
   com.mojang.blaze3d.font.GlyphBitmap bitmap=(com.mojang.blaze3d.font.GlyphBitmap)a[1];
   if(bitmap.getPixelWidth()!=(enabled.getBoolean(cfg)?48:16))
    throw new AssertionError("Wrong raster variant dimensions: "+bitmap.getPixelWidth());
   return java.lang.reflect.Proxy.newProxyInstance(loader,new Class[]{baked},(p2,m2,a2)->null);
  });
  java.lang.reflect.Field wrapped=fontSet.getDeclaredField("wrappedStitcher");wrapped.setAccessible(true);wrapped.set(owner,fakeStitcher);
  java.lang.reflect.Constructor<?> ctor=delayed.getDeclaredConstructors()[0];ctor.setAccessible(true);
  Object supplier=ctor.newInstance(owner,glyph);
  java.lang.reflect.Method get=delayed.getDeclaredMethod("get");get.setAccessible(true);
  Object[] variants=new Object[5];
  for(int pass=0;pass<3;pass++)for(int signature=0;signature<=4;signature++){
   enabled.setBoolean(cfg,signature!=0);pixels.setInt(cfg,Math.max(1,signature));
   Object result=get.invoke(supplier);
   if(pass==0)variants[signature]=result;
   else if(result!=variants[signature])throw new AssertionError("Variant rebaked on switch");
  }
  if(bakes[0]!=5)throw new AssertionError("Expected five lazy bakes, got "+bakes[0]);
  // Supported variants are now only native plus explicit 1/2/3/4px.
  // A real font reload creates a new DelayedBake: no stale texture survives.
  Object replacement=ctor.newInstance(owner,glyph);
  if(get.invoke(replacement)==variants[4]||bakes[0]!=6)throw new AssertionError("New atlas reused stale glyph");
  System.out.println("FONT_VARIANTS_SMOKE native+1/2/3/4 cached across switches; new atlas rebakes");
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
  // Ensure actual line positions change, not just the widget's height.
  java.lang.reflect.Field englishOnly=settings.getField("excludeEnglishFromSpacing");
  Object english=net.minecraft.util.FormattedCharSequence.forward(
      "Sodium settings",net.minecraft.network.chat.Style.EMPTY);
  Object japanese=net.minecraft.util.FormattedCharSequence.forward(
      "日本語設定",net.minecraft.network.chat.Style.EMPTY);
  Object englishRow=ctor.newInstance(english,6);
  Object japaneseRow=ctor.newInstance(japanese,6);
  factor.setDouble(cfg,2);
  englishOnly.setBoolean(cfg,true);
  rows.set(label,java.util.List.of(englishRow,japaneseRow));
  ys.clear();
  visit.invoke(label,left,0,10,9,collector);
  if(!ys.equals(java.util.List.of(10,19)))
      throw new AssertionError("English line must keep native pitch: "+ys);
  rows.set(label,java.util.List.of(japaneseRow,englishRow));
  ys.clear();
  visit.invoke(label,left,0,10,9,collector);
  if(!ys.equals(java.util.List.of(10,28)))
      throw new AssertionError("Japanese line must apply adjusted pitch: "+ys);
  englishOnly.setBoolean(cfg,false);
  factor.setDouble(cfg,1);
  System.out.println("FONT_SPACING_SMOKE labels=[10,19] -> [10,28]; English skip and Japanese pitch verified; tooltip="+normal+" -> "+scaled);
 }
}

