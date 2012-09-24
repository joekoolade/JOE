//===- MvmDenseSet.h - Dense probed hash set --------------------*- C++ -*-===//
//
//                     The LLVM Compiler Infrastructure
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//
//
// This file defines the MvmDenseSet class copied from llvm/ADT/DenseMap.h, but
// without storing pairs.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_DENSESET_H
#define MVM_DENSESET_H

#include "llvm/ADT/DenseMapInfo.h"
#include "llvm/Support/MathExtras.h"
#include "llvm/Support/PointerLikeTypeTraits.h"
#include "llvm/Support/type_traits.h"
#include <algorithm>
#include <iterator>
#include <new>
#include <utility>
#include <cassert>
#include <cstddef>
#include <cstring>

namespace mvm {

template<typename ValueT,
         typename ValueInfoT = MvmDenseMapInfo<ValueT>,
         bool IsConst = false>
class MvmDenseSetIterator;

template<typename KeyT, typename ValueT,
         typename KeyInfoT = MvmDenseMapInfo<KeyT>,
         typename ValueInfoT = MvmDenseMapInfo<ValueT> >
class MvmDenseSet {
public:
  typedef ValueT BucketT;
  uint32_t NumBuckets;
  BucketT *Buckets;

  uint32_t NumEntries;
  uint32_t NumTombstones;
  bool IsPrecompiled;

  typedef KeyT key_type;
  typedef ValueT mapped_type;
  typedef BucketT value_type;

  explicit MvmDenseSet(unsigned NumInitBuckets = 0) {
    IsPrecompiled = false;
    init(NumInitBuckets);
  }

  ~MvmDenseSet() {
    const ValueT EmptyValue = getEmptyValue(), TombstoneValue = getTombstoneValue();
    for (BucketT *P = Buckets, *E = Buckets+NumBuckets; P != E; ++P) {
      if (!ValueInfoT::isEqual(*P, EmptyValue) &&
          !ValueInfoT::isEqual(*P, TombstoneValue))
        (*P).~ValueT();
    }
#ifndef NDEBUG
    if (NumBuckets)
      memset((void*)Buckets, 0x5a, sizeof(BucketT)*NumBuckets);
#endif
    if (!IsPrecompiled) {
      operator delete(Buckets);
    }
  }

  typedef MvmDenseSetIterator<ValueT, ValueInfoT> iterator;
  typedef MvmDenseSetIterator<ValueT, ValueInfoT, true> const_iterator;
  inline iterator begin() {
    // When the map is empty, avoid the overhead of AdvancePastEmptyBuckets().
    return empty() ? end() : iterator(Buckets, Buckets+NumBuckets);
  }
  inline iterator end() {
    return iterator(Buckets+NumBuckets, Buckets+NumBuckets);
  }
  inline const_iterator begin() const {
    return empty() ? end() : const_iterator(Buckets, Buckets+NumBuckets);
  }
  inline const_iterator end() const {
    return const_iterator(Buckets+NumBuckets, Buckets+NumBuckets);
  }

  bool empty() const { return NumEntries == 0; }
  unsigned size() const { return NumEntries; }

  /// Grow the denseset so that it has at least Size buckets. Does not shrink
  void resize(size_t Size) {
    if (Size > NumBuckets)
      grow(Size);
  }

  void clear() {
    if (NumEntries == 0 && NumTombstones == 0) return;
    
    // If the capacity of the array is huge, and the # elements used is small,
    // shrink the array.
    if (NumEntries * 4 < NumBuckets && NumBuckets > 64) {
      shrink_and_clear();
      return;
    }

    const ValueT EmptyValue = getEmptyValue(), TombstoneValue = getTombstoneValue();
    for (BucketT *P = Buckets, *E = Buckets+NumBuckets; P != E; ++P) {
      if (!ValueInfoT::isEqual(*P, EmptyValue)) {
        if (!ValueInfoT::isEqual(*P, TombstoneValue)) {
          P->~ValueT();
          --NumEntries;
        }
        *P = EmptyValue;
      }
    }
    assert(NumEntries == 0 && "Node count imbalance!");
    NumTombstones = 0;
  }

  /// count - Return true if the specified key is in the set.
  bool count(const KeyT &Val) const {
    BucketT *TheBucket;
    return LookupBucketFor(Val, TheBucket);
  }

  iterator find(const KeyT &Val) {
    BucketT *TheBucket;
    if (LookupBucketFor(Val, TheBucket))
      return iterator(TheBucket, Buckets+NumBuckets);
    return end();
  }
  const_iterator find(const KeyT &Val) const {
    BucketT *TheBucket;
    if (LookupBucketFor(Val, TheBucket))
      return const_iterator(TheBucket, Buckets+NumBuckets);
    return end();
  }

  /// lookup - Return the entry for the specified key, or a default
  /// constructed value if no such entry exists.
  ValueT lookup(const KeyT &Val) const {
    BucketT *TheBucket;
    if (LookupBucketFor(Val, TheBucket))
      return *TheBucket;
    return ValueT();
  }

  // Inserts key,value pair into the map if the key isn't already in the map.
  // If the key is already in the map, it returns false and doesn't update the
  // value.
  std::pair<iterator, bool> insert(const std::pair<KeyT, ValueT> &KV) {
    BucketT *TheBucket;
    if (LookupBucketFor(KV.first, TheBucket))
      return std::make_pair(iterator(TheBucket, Buckets+NumBuckets),
                            false); // Already in map.

    // Otherwise, insert the new element.
    TheBucket = InsertIntoBucket(KV.first, KV.second, TheBucket);
    return std::make_pair(iterator(TheBucket, Buckets+NumBuckets),
                          true);
  }

  /// insert - Range insertion of pairs.
  template<typename InputIt>
  void insert(InputIt I, InputIt E) {
    for (; I != E; ++I)
      insert(*I);
  }


  bool erase(const KeyT &Val) {
    BucketT *TheBucket;
    if (!LookupBucketFor(Val, TheBucket))
      return false; // not in map.

    (*TheBucket).~ValueT();
    *TheBucket = getTombstoneValue();
    --NumEntries;
    ++NumTombstones;
    return true;
  }
  void erase(iterator I) {
    BucketT *TheBucket = &*I;
    (*TheBucket).~ValueT();
    *TheBucket = getTombstoneValue();
    --NumEntries;
    ++NumTombstones;
  }

  void swap(MvmDenseSet& RHS) {
    std::swap(NumBuckets, RHS.NumBuckets);
    std::swap(Buckets, RHS.Buckets);
    std::swap(NumEntries, RHS.NumEntries);
    std::swap(NumTombstones, RHS.NumTombstones);
  }

  value_type& FindAndConstruct(const KeyT &Key) {
    BucketT *TheBucket;
    if (LookupBucketFor(Key, TheBucket))
      return *TheBucket;

    return *InsertIntoBucket(Key, ValueT(), TheBucket);
  }

  ValueT &operator[](const KeyT &Key) {
    return FindAndConstruct(Key);
  }

  /// isPointerIntoBucketsArray - Return true if the specified pointer points
  /// somewhere into the MvmDenseSet's array of buckets.
  bool isPointerIntoBucketsArray(const void *Ptr) const {
    return Ptr >= Buckets && Ptr < Buckets+NumBuckets;
  }

  /// getPointerIntoBucketsArray() - Return an opaque pointer into the buckets
  /// array.  In conjunction with the previous method, this can be used to
  /// determine whether an insertion caused the MvmDenseSet to reallocate.
  const void *getPointerIntoBucketsArray() const { return Buckets; }

private:
  BucketT *InsertIntoBucket(const KeyT &Key, const ValueT &Value,
                            BucketT *TheBucket) {
    // If the load of the hash table is more than 3/4, or if fewer than 1/8 of
    // the buckets are empty (meaning that many are filled with tombstones),
    // grow the table.
    //
    // The later case is tricky.  For example, if we had one empty bucket with
    // tons of tombstones, failing lookups (e.g. for insertion) would have to
    // probe almost the entire table until it found the empty bucket.  If the
    // table completely filled with tombstones, no lookup would ever succeed,
    // causing infinite loops in lookup.
    ++NumEntries;
    if (NumEntries*4 >= NumBuckets*3) {
      this->grow(NumBuckets * 2);
      LookupBucketFor(Key, TheBucket);
    }
    if (NumBuckets-(NumEntries+NumTombstones) < NumBuckets/8) {
      this->grow(NumBuckets);
      LookupBucketFor(Key, TheBucket);
    }

    // If we are writing over a tombstone, remember this.
    if (!ValueInfoT::isEqual(*TheBucket, getEmptyValue()))
      --NumTombstones;

    new (TheBucket) ValueT(Value);
    return TheBucket;
  }

  static unsigned getHashValue(const KeyT &Val) {
    return KeyInfoT::getHashValue(Val);
  }
  static const ValueT getEmptyValue() {
    return ValueInfoT::getEmptyKey();
  }
  static const ValueT getTombstoneValue() {
    return ValueInfoT::getTombstoneKey();
  }

  /// LookupBucketFor - Lookup the appropriate bucket for Val, returning it in
  /// FoundBucket.  If the bucket contains the key and a value, this returns
  /// true, otherwise it returns a bucket with an empty marker or tombstone and
  /// returns false.
  bool LookupBucketFor(const KeyT &Key, BucketT *&FoundBucket) const {
    unsigned BucketNo = getHashValue(Key);
    unsigned ProbeAmt = 1;
    BucketT *BucketsPtr = Buckets;

    if (NumBuckets == 0) {
      FoundBucket = 0;
      return false;
    }

    // FoundTombstone - Keep track of whether we find a tombstone while probing.
    BucketT *FoundTombstone = 0;
    const ValueT EmptyValue = getEmptyValue();
    const ValueT TombstoneValue = getTombstoneValue();

    while (1) {
      BucketT *ThisBucket = BucketsPtr + (BucketNo & (NumBuckets-1));
      // Found Val's bucket?  If so, return it.
      if (ValueInfoT::isEqualKey(*ThisBucket, Key)) {
        FoundBucket = ThisBucket;
        return true;
      }

      // If we found an empty bucket, the key doesn't exist in the set.
      // Insert it and return the default value.
      if (ValueInfoT::isEqual(*ThisBucket, EmptyValue)) {
        // If we've already seen a tombstone while probing, fill it in instead
        // of the empty bucket we eventually probed to.
        if (FoundTombstone) ThisBucket = FoundTombstone;
        FoundBucket = FoundTombstone ? FoundTombstone : ThisBucket;
        return false;
      }

      // If this is a tombstone, remember it.  If Val ends up not in the map, we
      // prefer to return it than something that would require more probing.
      if (ValueInfoT::isEqual(*ThisBucket, TombstoneValue) && !FoundTombstone)
        FoundTombstone = ThisBucket;  // Remember the first tombstone found.

      // Otherwise, it's a hash collision or a tombstone, continue quadratic
      // probing.
      BucketNo += ProbeAmt++;
    }
  }

  void init(unsigned InitBuckets) {
    NumEntries = 0;
    NumTombstones = 0;
    NumBuckets = InitBuckets;

    if (InitBuckets == 0) {
      Buckets = 0;
      return;
    }

    assert(InitBuckets && (InitBuckets & (InitBuckets-1)) == 0 &&
           "# initial buckets must be a power of two!");
    Buckets = static_cast<BucketT*>(operator new(sizeof(BucketT)*InitBuckets));
    // Initialize all the entries to EmptyValue.
    const ValueT EmptyValue = getEmptyValue();
    for (unsigned i = 0; i != InitBuckets; ++i)
      new (&Buckets[i]) ValueT(EmptyValue);
  }

  void grow(unsigned AtLeast) {
    unsigned OldNumBuckets = NumBuckets;
    BucketT *OldBuckets = Buckets;

    if (NumBuckets < 64)
      NumBuckets = 64;

    // Double the number of buckets.
    while (NumBuckets < AtLeast)
      NumBuckets <<= 1;
    NumTombstones = 0;
    Buckets = static_cast<BucketT*>(operator new(sizeof(BucketT)*NumBuckets));

    // Initialize all the values to EmptyValue.
    const ValueT EmptyValue = getEmptyValue();
    for (unsigned i = 0, e = NumBuckets; i != e; ++i)
      new (&Buckets[i]) ValueT(EmptyValue);

    // Insert all the old elements.
    const ValueT TombstoneValue = getTombstoneValue();
    for (BucketT *B = OldBuckets, *E = OldBuckets+OldNumBuckets; B != E; ++B) {
      if (!ValueInfoT::isEqual(*B, EmptyValue) &&
          !ValueInfoT::isEqual(*B, TombstoneValue)) {
        // Insert the value into the new table.
        BucketT *DestBucket;
        KeyT key = ValueInfoT::toKey(*B);
        bool FoundVal = LookupBucketFor(key, DestBucket);
        (void)FoundVal; // silence warning.
        assert(!FoundVal && "Key already in new map?");
        new (DestBucket) ValueT(*B);

        // Free the value.
        (*B).~ValueT();
      }
    }

#ifndef NDEBUG
    if (OldNumBuckets)
      memset((void*)OldBuckets, 0x5a, sizeof(BucketT)*OldNumBuckets);
#endif
    // Free the old table.
    if (!IsPrecompiled) {
      operator delete(OldBuckets);
    } else {
      IsPrecompiled = false;
    }
  }

  void shrink_and_clear() {
    unsigned OldNumBuckets = NumBuckets;
    BucketT *OldBuckets = Buckets;

    // Reduce the number of buckets.
    NumBuckets = NumEntries > 32 ? 1 << (llvm::Log2_32_Ceil(NumEntries) + 1)
                                 : 64;
    NumTombstones = 0;
    Buckets = static_cast<BucketT*>(operator new(sizeof(BucketT)*NumBuckets));

    // Initialize all the entries to EmptyValue.
    const ValueT EmptyValue = getEmptyValue();
    for (unsigned i = 0, e = NumBuckets; i != e; ++i)
      new (&Buckets[i]) ValueT(EmptyValue);

    // Free the old buckets.
    const ValueT TombstoneValue = getTombstoneValue();
    for (BucketT *B = OldBuckets, *E = OldBuckets+OldNumBuckets; B != E; ++B) {
      if (!ValueInfoT::isEqual(*B, EmptyValue) &&
          !ValueInfoT::isEqual(*B, TombstoneValue)) {
        // Free the value.
        (*B).~ValueT();
      }
    }

#ifndef NDEBUG
    memset((void*)OldBuckets, 0x5a, sizeof(BucketT)*OldNumBuckets);
#endif
    if (!IsPrecompiled) {
      // Free the old table.
      operator delete(OldBuckets);
    } else {
      IsPrecompiled = false;
    }

    NumEntries = 0;
  }
  
public:
  /// Return the approximate size (in bytes) of the actual map.
  /// This is just the raw memory used by MvmDenseSet.
  /// If entries are pointers to objects, the size of the referenced objects
  /// are not included.
  size_t getMemorySize() const {
    return NumBuckets * sizeof(BucketT);
  }
};

template<typename ValueT,
         typename ValueInfoT, bool IsConst>
class MvmDenseSetIterator {
  typedef ValueT Bucket;
  typedef MvmDenseSetIterator<ValueT, ValueInfoT, true> ConstIterator;
  friend class MvmDenseSetIterator<ValueT, ValueInfoT, true>;
public:
  typedef ptrdiff_t difference_type;
  typedef typename llvm::conditional<IsConst, const Bucket, Bucket>::type value_type;
  typedef value_type *pointer;
  typedef value_type &reference;
  typedef std::forward_iterator_tag iterator_category;
private:
  pointer Ptr, End;
public:
  MvmDenseSetIterator() : Ptr(0), End(0) {}

  MvmDenseSetIterator(pointer Pos, pointer E) : Ptr(Pos), End(E) {
    AdvancePastEmptyBuckets();
  }

  // If IsConst is true this is a converting constructor from iterator to
  // const_iterator and the default copy constructor is used.
  // Otherwise this is a copy constructor for iterator.
  MvmDenseSetIterator(const MvmDenseSetIterator<ValueT, ValueInfoT, false>& I)
    : Ptr(I.Ptr), End(I.End) {}

  reference operator*() const {
    return *Ptr;
  }
  pointer operator->() const {
    return Ptr;
  }

  bool operator==(const ConstIterator &RHS) const {
    return Ptr == RHS.operator->();
  }
  bool operator!=(const ConstIterator &RHS) const {
    return Ptr != RHS.operator->();
  }

  inline MvmDenseSetIterator& operator++() {  // Preincrement
    ++Ptr;
    AdvancePastEmptyBuckets();
    return *this;
  }
  MvmDenseSetIterator operator++(int) {  // Postincrement
    MvmDenseSetIterator tmp = *this; ++*this; return tmp;
  }

private:
  void AdvancePastEmptyBuckets() {
    const ValueT Empty = ValueInfoT::getEmptyKey();
    const ValueT Tombstone = ValueInfoT::getTombstoneKey();

    while (Ptr != End &&
           (ValueInfoT::isEqual(*Ptr, Empty) ||
            ValueInfoT::isEqual(*Ptr, Tombstone)))
      ++Ptr;
  }
};

} // end namespace mvm

#endif
