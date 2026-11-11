"""After Maven package, verify CERES configuration and mapper class references offline."""
from pathlib import Path
import zipfile
import tempfile, atexit, shutil
root=Path(__file__).resolve().parents[1] / 'backend-system'
tmp=Path(tempfile.mkdtemp(prefix='ceres-java-check-'))
atexit.register(shutil.rmtree, tmp, ignore_errors=True)
with zipfile.ZipFile(root/'ceres-admin/target/ceres-admin.jar') as z:
 for n in z.namelist():
  if n.startswith('BOOT-INF/lib/') and n.endswith('.jar'):
   (tmp/Path(n).name).write_bytes(z.read(n))
source='''import com.ceres.common.config.CeresConfig;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import java.nio.file.*;
import java.io.*;

public class CeresMigrationCheck {
    public static void main(String[] args) throws Exception {
        StandardEnvironment env = new StandardEnvironment();
        new YamlPropertySourceLoader().load("ceres", new FileSystemResource(args[0])).forEach(p -> env.getPropertySources().addLast(p));
        CeresConfig config = new CeresConfig();
        Binder.get(env).bind("ceres", Bindable.ofInstance(config));
        if (!"CERES".equals(config.getName()) || !env.getProperty("ceres.profile").equals(CeresConfig.getProfile()) || !"math".equals(CeresConfig.getCaptchaType())) {
            throw new IllegalStateException("CERES configuration binding failed");
        }
        Configuration mybatis = new Configuration();
        mybatis.getTypeAliasRegistry().registerAliases("com.ceres.system.domain");
        mybatis.getTypeAliasRegistry().registerAliases("com.ceres.common.core.domain");
        mybatis.getTypeAliasRegistry().registerAliases("com.ceres.common.core.domain.entity");
        final int[] count = {0};
        try (java.util.stream.Stream<Path> paths = Files.walk(Paths.get(args[1]))) {
            paths.filter(p -> p.toString().endsWith(".xml")).forEach(p -> {
                try (InputStream in = Files.newInputStream(p)) {
                    new XMLMapperBuilder(in, mybatis, p.toString(), mybatis.getSqlFragments()).parse();
                    count[0]++;
                } catch (Exception e) { throw new RuntimeException(p.toString(), e); }
            });
        }
        mybatis.getMappedStatementNames();
        System.out.println("Spring CERES binding passed; MyBatis mapper files parsed: " + count[0]);
    }
}
'''
(tmp/'CeresMigrationCheck.java').write_text(source)
classpath=str(tmp)+'/*:'+':'.join(str(root/m/'target/classes') for m in ['ceres-admin','ceres-common','ceres-system','ceres-framework'])+':'+str(tmp)
import subprocess
subprocess.run(['javac','-cp',classpath,str(tmp/'CeresMigrationCheck.java')],check=True)
subprocess.run(['java','-cp',classpath,'CeresMigrationCheck',str(root/'ceres-admin/src/main/resources/application.yml'),str(root/'ceres-system/src/main/resources/mapper')],check=True,stderr=subprocess.STDOUT)
print('Spring binding and MyBatis mapper check passed')
