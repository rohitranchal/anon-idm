
<?php
$path = '/Users/ruchith/Documents/research/anon_idm/source/java/scripts/wallet';
$target = '/Users/ruchith/Documents/research/anon_idm/source/js/sp1/claim_defs';
if ($handle = opendir($path)) {
    while (false !== ($entry = readdir($handle))) {
        if ($entry != "." && $entry != "..") {
            $contents = file_get_contents($path . '/' . $entry);
            $contents = json_decode($contents);
            $name = $contents->claimDef->name;
            $v = json_encode($contents->claimDef);
            print_r($name . "\n");
            file_put_contents($target . '/' . $name, $v);
        }
    }
    closedir($handle);
}
?>
