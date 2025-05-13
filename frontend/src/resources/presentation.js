import en_01 from "@/md_files/presentation/en/01_introduction.md";
import en_02 from "@/md_files/presentation/en/02_features_overview.md";
import en_03 from "@/md_files/presentation/en/03_functionality_and_usability.md";
import en_04 from "@/md_files/presentation/en/04_support_and_faq.md";
import en_05 from "@/md_files/presentation/en/05_closing_remarks.md";

import cs_01 from "@/md_files/presentation/cs/01_uvod.md";
import cs_02 from "@/md_files/presentation/cs/02_prehled_funkci.md";
import cs_03 from "@/md_files/presentation/cs/03_funkcnost_a_pouzitelnost.md";
import cs_04 from "@/md_files/presentation/cs/04_podpora_a_faq.md";
import cs_05 from "@/md_files/presentation/cs/05_zaverecne_poznamky.md";

const presentation = {
    id: 0, nameEn: 'Project Presentation', nameCz: 'Prezentace projektu',
    mdFilesEn: [
        {id: 0, name: 'Introduction', content: en_01},
        {id: 1, name: 'Features Overview', content: en_02},
        {id: 2, name: 'Functionality and Usability', content: en_03},
        {id: 3, name: 'Support and FAQ', content: en_04},
        {id: 4, name: 'Closing Remarks', content: en_05}
    ],
    mdFilesCz: [
        {id: 0, name: 'Úvod', content: cs_01},
        {id: 1, name: 'Přehled funkcí', content: cs_02},
        {id: 2, name: 'Funkčnost a použitelnost', content: cs_03},
        {id: 3, name: 'Podpora a FAQ', content: cs_04},
        {id: 4, name: 'Závěrečné poznámky', content: cs_05}
    ]
}

export default presentation