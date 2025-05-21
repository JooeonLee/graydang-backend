package com.graydang.app;

//@Component
//@RequiredArgsConstructor
//public class BillInitialLoader implements CommandLineRunner {
//
//    private final BillApiClient billApiClient;
//    private final BillService billService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        int pageNo = 1;
//        int numOfRows = 1;
//
//        List<BillInfoResponseDto.ItemDto> items = billApiClient.getBillInfoList(numOfRows, pageNo);
//
//        for (BillInfoResponseDto.ItemDto item : items) {
//            billService.saveOrUpdate(item);
//        }
//    }
//}
