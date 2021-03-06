// RUN: %clang_cc1 -fms-extensions -fblocks -emit-llvm %s -o - -cxx-abi microsoft -triple=i386-pc-win32 | FileCheck %s

void f1(const char* a, const char* b) {}
// CHECK: "\01?f1@@YAXPBD0@Z"

void f2(const char* a, char* b) {}
// CHECK: "\01?f2@@YAXPBDPAD@Z"

void f3(int a, const char* b, const char* c) {}
// CHECK: "\01?f3@@YAXHPBD0@Z"

const char *f4(const char* a, const char* b) { return 0; }
// CHECK: "\01?f4@@YAPBDPBD0@Z"

void f5(char const* a, unsigned int b, char c, void const* d, char const* e, unsigned int f) {}
// CHECK: "\01?f5@@YAXPBDIDPBX0I@Z"

void f6(bool a, bool b) {}
// CHECK: "\01?f6@@YAX_N0@Z"

void f7(int a, int* b, int c, int* d, bool e, bool f, bool* g) {}
// CHECK: "\01?f7@@YAXHPAHH0_N1PA_N@Z"

// FIXME: tests for more than 10 types?

struct S {
  void mbb(bool a, bool b) {}
};

void g1(struct S a) {}
// CHECK: "\01?g1@@YAXUS@@@Z"

void g2(struct S a, struct S b) {}
// CHECK: "\01?g2@@YAXUS@@0@Z"

void g3(struct S a, struct S b, struct S* c, struct S* d) {}
// CHECK: "\01?g3@@YAXUS@@0PAU1@1@Z"

void g4(const char* a, struct S* b, const char* c, struct S* d) {
// CHECK: "\01?g4@@YAXPBDPAUS@@01@Z"
  b->mbb(false, false);
// CHECK: "\01?mbb@S@@QAEX_N0@Z"
}

// Make sure that different aliases of built-in types end up mangled as the
// built-ins.
typedef unsigned int uintptr_t;
typedef unsigned int size_t;
void *h(size_t a, uintptr_t b) { return 0; }
// CHECK: "\01?h@@YAPAXII@Z"

// Function pointers might be mangled in a complex way.
typedef void (*VoidFunc)();
typedef int* (*PInt3Func)(int* a, int* b);

void h1(const char* a, const char* b, VoidFunc c, VoidFunc d) {}
// CHECK: "\01?h1@@YAXPBD0P6AXXZ1@Z"

void h2(void (*f_ptr)(void *), void *arg) {}
// CHECK: "\01?h2@@YAXP6AXPAX@Z0@Z"

PInt3Func h3(PInt3Func x, PInt3Func y, int* z) { return 0; }
// CHECK: "\01?h3@@YAP6APAHPAH0@ZP6APAH00@Z10@Z"

namespace foo {
void foo() { }
// CHECK: "\01?foo@0@YAXXZ"
}
